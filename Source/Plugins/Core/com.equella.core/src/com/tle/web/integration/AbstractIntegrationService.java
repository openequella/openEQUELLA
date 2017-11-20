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

package com.tle.web.integration;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.item.service.ItemResolver;
import com.tle.core.plugins.PluginTracker;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.layout.LayoutSelector;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionSession;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.MenuMode;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ItemUrlExtender;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@NonNullByDefault
public abstract class AbstractIntegrationService<T extends IntegrationSessionData> implements Integration<T>
{
	/**
	 * Magic key to say that extension applies to all types of integration
	 */
	private static final String ALL_TYPES_KEY = "all";

	@Inject
	protected AttachmentResourceService attachmentResourceService;
	@Inject
	protected ItemResolver itemResolver;
	@Inject
	private PluginTracker<IntegrationSessionExtension> resultsTracker;
	/* @LazyNonNull */@Nullable
	private volatile SetMultimap<String, IntegrationSessionExtension> extensionMap;

	protected abstract boolean canSelect(T data);

	protected abstract String getIntegrationType();

	protected abstract <I extends IItem<?>> ViewableItem<I> createViewableItem(I item, SelectedResource resource);

	private SetMultimap<String, IntegrationSessionExtension> getExtensionMap()
	{
		if( extensionMap == null )
		{
			synchronized( this )
			{
				if( extensionMap == null )
				{
					final SetMultimap<String, IntegrationSessionExtension> map = HashMultimap
						.<String, IntegrationSessionExtension>create();
					for( Extension ext : resultsTracker.getExtensions() )
					{
						final IntegrationSessionExtension integExtension = resultsTracker.getBeanByExtension(ext);
						for( Parameter parameter : ext.getParameters("type") )
						{
							map.put(parameter.valueAsString(), integExtension);
						}
					}
					extensionMap = Multimaps.unmodifiableSetMultimap(map);
				}
			}
		}
		return extensionMap;
	}

	protected Set<IntegrationSessionExtension> getExtensions()
	{
		final SetMultimap<String, IntegrationSessionExtension> map = getExtensionMap();
		final Set<IntegrationSessionExtension> typeExtensions = Sets.newHashSet(map.get(getIntegrationType()));
		typeExtensions.addAll(map.get(ALL_TYPES_KEY));
		return typeExtensions;
	}

	@Override
	public void setupSingleSignOn(SectionInfo info, SingleSignonForm form) throws Exception
	{
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public SelectionSession setupSelectionSession(SectionInfo info, T data, SelectionSession session,
		SingleSignonForm model)
	{
		if( !canSelect(data) )
		{
			return null;
		}
		// These are mutually exclusive, it doesn't make sense to mix and match
		// them. Ideally they should just be toggles, but we have to live with
		// what's already out there.
		boolean selectItem = true;
		boolean selectAttachment = true;
		boolean selectPackage = true;
		if( model.isItemonly() )
		{
			selectAttachment = false;
			selectPackage = false;
		}
		else if( model.isAttachmentonly() )
		{
			selectItem = false;
			selectPackage = false;
		}
		else if( model.isPackageonly() )
		{
			selectItem = false;
			selectAttachment = false;
		}
		session.setSelectItem(selectItem);
		session.setSelectAttachments(selectAttachment);
		session.setSelectPackage(selectPackage);

		for( IntegrationSessionExtension ext : getExtensions() )
		{
			ext.setupSession(info, session, model);
		}
		return session;
	}

	@Override
	public void forward(SectionInfo info, T data, SectionInfo forward)
	{
		info.forward(forward);
	}

	protected IItem<?> getItemForResource(SelectedResource resource)
	{
		final String uuid = resource.getUuid();
		final String extensionType = resource.getKey().getExtensionType();
		final ItemId itemId;
		if( resource.isLatest() )
		{
			final int latestVersion = itemResolver.getLiveItemVersion(uuid, extensionType);
			itemId = new ItemId(uuid, latestVersion);
		}
		else
		{
			itemId = new ItemId(uuid, resource.getVersion());
		}
		final IItem<?> item = itemResolver.getItem(itemId, extensionType);
		if( item == null )
		{
			throw new RuntimeException(CurrentLocale.get("com.tle.web.integration.error.noitemforresource",
				resource.getUuid(), resource.getVersion()));
		}
		return item;
	}

	protected SelectedResource getFirstSelectedResource(SelectionSession session)
	{
		return session.getSelectedResources().iterator().next();
	}

	@Override
	public LayoutSelector createLayoutSelector(SectionInfo info, T data)
	{
		return new LayoutSelector()
		{
			@Override
			public TemplateResult getLayout(Decorations decorations, RenderContext info, TemplateResult templateResult)
				throws Exception
			{
				return null;
			}

			@Override
			public void preProcess(Decorations decorations)
			{
				decorations.setMenuMode(MenuMode.HIDDEN);
				decorations.setBanner(false);
				decorations.setContent(true);
			}
		};
	}

	@Override
	public LmsLinkInfo getLinkForResource(SectionInfo info, ViewableItem<? extends IItem<?>> vitem,
		SelectedResource resource, boolean relative, boolean attachmentUuidUrls)
	{
		IItem<?> item = vitem.getItem();
		String title = resource.getTitle();
		if( Check.isEmpty(title) )
		{
			title = CurrentLocale.get(item.getName(), item.getUuid());
		}

		String description = resource.getDescription();
		if( description == null )
		{
			description = "";
		}

		ViewableResource viewableResource = null;

		final String url = resource.getUrl();
		final String attachmentUuid = resource.getAttachmentUuid();
		final IAttachment attach = (attachmentUuid == null ? null
			: new UnmodifiableAttachments(item).getAttachmentByUuid(attachmentUuid));
		switch( resource.getType() )
		{
			case SelectedResource.TYPE_REMOTE:
				return new LmsLinkInfo(item, attach, new LmsLink(url, title, description, attachmentUuid));

			case SelectedResource.TYPE_ATTACHMENT:
				viewableResource = attachmentResourceService.getViewableResource(info, vitem, attach);
				break;

			case SelectedResource.TYPE_PATH:
				viewableResource = attachmentResourceService.createPathResource(info, vitem, url, attach);
				if( url.length() == 0 && description.length() == 0 )
				{
					description = CurrentLocale.get(item.getDescription(), "");
				}
				break;

			default:
				throw new RuntimeException("Unexpected resource type '" + resource.getType() + "'");
		}

		if( attachmentUuidUrls )
		{
			viewableResource.setAttribute(ViewableResource.KEY_NO_FILE_PATHS, true);
		}

		ViewItemUrl vurl = viewableResource.createDefaultViewerUrl();
		vurl.addFlag(ViewItemUrl.FLAG_IGNORE_TRANSIENT);
		List<ItemUrlExtender> extenders = resource.getExtenders();
		if( extenders != null )
		{
			vurl.addAll(extenders);
		}

		String actualUrl;
		if( relative )
		{
			UrlEncodedString filepath = vurl.getFilepath();
			String query = vurl.getQueryString();
			if( !Check.isEmpty(query) )
			{
				actualUrl = filepath.toString() + '?' + query;
			}
			else
			{
				actualUrl = filepath.toString();
			}
		}
		else
		{
			vurl.addFlag(ViewItemUrl.FLAG_FULL_URL);
			actualUrl = vurl.getHref();
		}

		return new LmsLinkInfo(viewableResource.getViewableItem().getItem(), viewableResource.getAttachment(),
			new LmsLink(actualUrl, title, description, attachmentUuid));
	}
}
