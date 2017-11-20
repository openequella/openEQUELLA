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

package com.tle.web.htmleditor.tinymce.addon.tle.service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.dytech.edge.common.Constants;
import com.google.common.cache.CacheLoader;
import com.tle.beans.Institution;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.mimetypes.MimeTypesSearchResults;
import com.tle.core.mimetypes.MimeTypesUpdatedListener;
import com.tle.web.freemarker.FreemarkerSectionResult;
import com.tle.web.htmleditor.tinymce.addon.tle.MimeTemplateFreemarkerFactory;
import com.tle.web.htmleditor.tinymce.addon.tle.TinyMceAddonConstants;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionWriter;
import com.tle.web.viewurl.ResourceViewer;
import com.tle.web.viewurl.ViewItemService;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind(MimeTemplateService.class)
@Singleton
public class MimeTemplateServiceImpl implements MimeTemplateService, MimeTypesUpdatedListener
{
	private static final Logger LOGGER = Logger.getLogger(MimeTemplateService.class);

	@Inject
	private RunAsInstitution runAs;
	@Inject
	private MimeTypeService mimeService;
	@Inject
	private MimeTemplateFreemarkerFactory custFactory;
	@Inject
	private ViewItemService viewItemService;

	private InstitutionCache<Set<String>> embeddableCache;

	@Inject
	public void setInstitutionService(InstitutionService service)
	{
		embeddableCache = service.newInstitutionAwareCache(new CacheLoader<Institution, Set<String>>()
		{
			@Override
			public Set<String> load(Institution key)
			{
				return fillCache();
			}
		});
	}

	@Override
	public String getTemplateForMimeType(String mime)
	{
		MimeEntry mimeEntry = mimeService.getEntryForMimeType(mime);
		if( mimeEntry != null )
		{
			return getTemplateForMimeEntry(mimeEntry);
		}
		return null;
	}

	@Override
	public String getTemplateForMimeEntry(MimeEntry mimeEntry)
	{
		return mimeEntry.getAttributes().get(TinyMceAddonConstants.MIME_TEMPLATE_KEY);
	}

	@Override
	public String getPopulatedTemplate(SectionInfo info, ViewableResource vres, String title)
	{
		final String template = getTemplateForMimeType(vres.getMimeType());
		// replace url and title
		if( template != null )
		{
			final String vhref = vres.createDefaultViewerUrl()
				.addFlag(ViewItemUrl.FLAG_FULL_URL | ViewItemUrl.FLAG_IGNORE_TRANSIENT).getHref();

			final FreemarkerSectionResult res = custFactory.createResult("mimetemplate", new StringReader(template),
				null);
			// These 3 are more or less deprecated now. Use the 'resource'
			// variable.
			res.addExtraObject("url", vres.createCanonicalUrl().getHref());
			res.addExtraObject("vurl", vhref);
			res.addExtraObject("title", title);
			res.addExtraObject("resource", new ResourceWrapper(vres, title));

			final StringWriter outbuf = new StringWriter();
			try
			{
				res.realRender(new SectionWriter(outbuf, info.getRootRenderContext()));
			}
			catch( IOException io )
			{
				LOGGER.error("Error rendering MIME template", io);
				return null;
			}
			return outbuf.toString();
		}
		return null;
	}

	@Override
	public Collection<String> getEmbeddableMimeTypes()
	{
		return embeddableCache.getCache();
	}

	protected Set<String> fillCache()
	{
		final Set<String> embeds = new HashSet<String>();
		runAs.executeAsSystem(CurrentInstitution.get(), new Runnable()
		{
			@Override
			public void run()
			{
				MimeTypesSearchResults results = mimeService.searchByMimeType(Constants.BLANK, 0, -1);
				for( MimeEntry entry : results.getResults() )
				{
					String template = getTemplateForMimeEntry(entry);
					if( !Check.isEmpty(template) )
					{
						embeds.add(entry.getType());
					}
				}
			}
		});
		return embeds;
	}

	@Override
	public void clearMimeCache()
	{
		embeddableCache.clear();
	}

	public class ResourceWrapper
	{
		private final ViewableResource vres;
		private final String title;

		protected ResourceWrapper(ViewableResource vres, String title)
		{
			this.vres = vres;
			this.title = title;
		}

		public String getDefaultViewerUrl()
		{
			return vres.createDefaultViewerUrl().addFlag(ViewItemUrl.FLAG_FULL_URL | ViewItemUrl.FLAG_IGNORE_TRANSIENT)
				.getHref();
		}

		public String getCanonicalUrl()
		{
			return vres.createCanonicalUrl().getHref();
		}

		public String getViewerUrl(String viewer)
		{
			ResourceViewer resourceViewer = viewItemService.getViewer(viewer);
			if( resourceViewer != null )
			{
				return resourceViewer.createViewItemUrl(vres.getInfo(), vres)
					.addFlag(ViewItemUrl.FLAG_FULL_URL | ViewItemUrl.FLAG_IGNORE_TRANSIENT).getHref();
			}
			return "";
		}

		public String getTitle()
		{
			return title;
		}
	}
}
