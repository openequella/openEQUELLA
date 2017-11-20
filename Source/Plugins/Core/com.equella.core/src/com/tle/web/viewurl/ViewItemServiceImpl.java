/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.viewurl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.dytech.edge.exceptions.ItemNotFoundException;
import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.collection.event.listener.ItemDefinitionDeletionListener;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(ViewItemService.class)
@Singleton
public class ViewItemServiceImpl implements ViewItemService, ItemDefinitionDeletionListener
{
	private final Cache<CachedTreeKey, CachedTree> sectionCache = CacheBuilder.newBuilder().build();

	private PluginTracker<ResourceViewer> viewerPlugins;
	private PluginTracker<ResourceViewerFilter> filterTracker;

	@Inject
	private MimeTypeService mimeTypeService;

	@Override
	public List<NameValue> getViewerNames()
	{
		List<NameValue> viewers = Lists
			.newArrayList(Lists.transform(viewerPlugins.getExtensions(), new Function<Extension, NameValue>()
			{
				@Override
				public NameValue apply(Extension o)
				{
					String n = CurrentLocale.get(o.getParameter("nameKey").valueAsString());
					String v = o.getParameter("id").valueAsString();
					return new NameValue(n, v);
				}
			}));
		Collections.sort(viewers, new Comparator<NameValue>()
		{
			private final Map<String, Extension> extensions = viewerPlugins.getExtensionMap();

			@Override
			public int compare(NameValue o1, NameValue o2)
			{
				// Use order if it exists otherwise fall back to name
				Parameter p1 = extensions.get(o1.getValue()).getParameter("order");
				Parameter p2 = extensions.get(o2.getValue()).getParameter("order");
				if( p1 != null && p2 != null )
				{
					Integer x = p1.valueAsNumber().intValue();
					Integer y = p2.valueAsNumber().intValue();
					return Integer.compare(x, y);
				}
				return Format.NAME_VALUE_COMPARATOR.compare(o1, o2);
			}
		});
		return viewers;
	}

	@Override
	public String getViewerNameKey(String viewerId)
	{
		Extension extension = viewerPlugins.getExtensionMap().get(viewerId);
		return extension.getParameter("nameKey").valueAsString();
	}

	@Override
	public String getViewerLinkKey(String viewerId)
	{
		Parameter parameter = viewerPlugins.getExtensionMap().get(viewerId).getParameter("linkKey");
		return parameter == null ? null : parameter.valueAsString();
	}

	@Nullable
	@Override
	public ResourceViewer getViewer(String viewerId)
	{
		Extension ext = viewerPlugins.getExtension(viewerId);
		if( ext != null )
		{
			return viewerPlugins.getBeanByExtension(ext);
		}
		return null;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		viewerPlugins = new PluginTracker<ResourceViewer>(pluginService, "com.tle.web.viewurl", "resourceViewer", "id",
			new PluginTracker.ExtensionParamComparator("order", false));
		viewerPlugins.setBeanKey("class");
		filterTracker = new PluginTracker<ResourceViewerFilter>(pluginService, "com.tle.web.viewurl", "resourceViewerFilter",
			"id");
		filterTracker.setBeanKey("class");
	}

	@Override
	public List<NameValue> getEnabledViewers(SectionInfo info, ViewableResource resource)
	{
		final List<NameValue> enabledList = new ArrayList<NameValue>();
		final MimeEntry mimeEntry = mimeTypeService.getEntryForMimeType(resource.getMimeType());
		final Set<String> enabledViewers;
		if( mimeEntry != null )
		{
			enabledViewers = new HashSet<String>(
				mimeTypeService.getListFromAttribute(mimeEntry, MimeTypeConstants.KEY_ENABLED_VIEWERS, String.class));
		}
		else
		{
			enabledViewers = new HashSet<>();
		}

		if( mimeEntry == null || !mimeEntry.getAttributes().containsKey(MimeTypeConstants.KEY_DISABLE_FILEVIEWER) )
		{
			enabledViewers.add(MimeTypeConstants.VAL_DEFAULT_VIEWERID);
		}
		List<NameValue> names = getViewerNames();
		for( NameValue viewerNv : names )
		{
			String viewerId = viewerNv.getValue();
			if( enabledViewers.contains(viewerId) )
			{
				ResourceViewer viewer = getViewer(viewerId);
				if( viewer != null && viewer.supports(info, resource) )
				{
					enabledList.add(viewerNv);
				}
			}
		}
		return enabledList;
	}

	@Override
	public String getDefaultViewerId(String mimeType)
	{
		return getDefaultViewerId(mimeTypeService.getEntryForMimeType(mimeType));
	}

	private String getDefaultViewerId(@Nullable MimeEntry entry)
	{
		String viewerId = null;
		if( entry != null )
		{
			viewerId = entry.getAttributes().get(MimeTypeConstants.KEY_DEFAULT_VIEWERID);
		}
		if( viewerId == null || viewerId.length() == 0 )
		{
			return MimeTypeConstants.VAL_DEFAULT_VIEWERID;
		}
		return viewerId;
	}

	@Override
	public LinkTagRenderer getViewableLink(SectionInfo info, ViewableResource resource, String viewerId)
	{
		final ResourceViewer viewer = getViewer(
			Check.isEmpty(viewerId) ? getDefaultViewerId(resource.getMimeType()) : viewerId);
		if( viewer != null )
		{
			try
			{
				final LinkTagRenderer linkTag = viewer.createLinkRenderer(info, resource);
				return filterLinkTag(info, resource, viewer, linkTag);
			}
			catch( ItemNotFoundException infe )
			{
				// No item found, render a dead link
				final HtmlLinkState linkState = new HtmlLinkState(new TextLabel(resource.getDescription()));
				return new LinkRenderer(linkState);
			}
		}
		// No viewer found, render a dead link
		final HtmlLinkState linkState = new HtmlLinkState(new TextLabel(resource.getDescription()));
		return new LinkRenderer(linkState);
	}

	private LinkTagRenderer filterLinkTag(SectionInfo info, ViewableResource resource, ResourceViewer viewer,
		LinkTagRenderer linkTag)
	{
		LinkTagRenderer tag = linkTag;
		final Collection<ResourceViewerFilter> filters = filterTracker.getBeanList();
		for( ResourceViewerFilter filter : filters )
		{
			tag = filter.filterLink(info, tag, viewer, resource);
		}
		return tag;
	}

	@Override
	public String getDefaultViewerId(ViewableResource resource)
	{
		String defaultViewer = resource.getDefaultViewer();
		return !Check.isEmpty(defaultViewer) ? defaultViewer : getDefaultViewerId(resource.getMimeType());
	}

	@Override
	public CachedTree getCachedTree(ItemDefinition collection)
	{
		long uniqueId = collection.getInstitution().getUniqueId();
		String collectionUuid = collection.getUuid();
		return sectionCache.getIfPresent(new CachedTreeKey(uniqueId, collectionUuid));
	}

	@Override
	public void putCachedTree(ItemDefinition collection, CachedTree cachedTree)
	{
		long uniqueId = collection.getInstitution().getUniqueId();
		String collectionUuid = collection.getUuid();
		CachedTreeKey key = new CachedTreeKey(uniqueId, collectionUuid);
		sectionCache.put(key, cachedTree);
	}

	@Override
	public void removeReferences(ItemDefinition collection)
	{
		long uniqueId = collection.getInstitution().getUniqueId();
		String collectionUuid = collection.getUuid();
		sectionCache.invalidate(new CachedTreeKey(uniqueId, collectionUuid));
	}

	private static class CachedTreeKey
	{
		private final long institution;
		private final String collectionUuid;

		public CachedTreeKey(long institution, String collectionUuid)
		{
			this.institution = institution;
			this.collectionUuid = collectionUuid;
		}

		@Override
		public boolean equals(Object obj)
		{
			if( obj instanceof CachedTreeKey )
			{
				CachedTreeKey other = (CachedTreeKey) obj;
				return collectionUuid.equals(other.collectionUuid) && institution == other.institution;
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return (int) institution + collectionUuid.hashCode();
		}
	}

	public static class CachedTree
	{
		private final Date lastModified;
		private final SectionTree tree;

		public CachedTree(Date lastModified, SectionTree tree)
		{
			this.lastModified = lastModified;
			this.tree = tree;
		}

		public Date getLastModified()
		{
			return lastModified;
		}

		public SectionTree getTree()
		{
			return tree;
		}
	}

}
