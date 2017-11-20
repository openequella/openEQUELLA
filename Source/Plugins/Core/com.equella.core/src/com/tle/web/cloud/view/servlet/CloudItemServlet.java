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

package com.tle.web.cloud.view.servlet;

import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.core.cloud.service.CloudService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.UrlService;
import com.tle.web.cloud.view.CloudViewableItem;
import com.tle.web.cloud.view.section.RootCloudViewItemSection;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.registry.TreeRegistry;
import com.tle.web.sections.render.Label;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
@Singleton
public class CloudItemServlet extends HttpServlet
{
	@PlugKey("viewitem.error.notfound.remoteserver")
	private static Label LABEL_ITEM_NOT_FOUND;

	static
	{
		PluginResourceHandler.init(CloudItemServlet.class);
	}

	@Inject
	private InstitutionService institutionService;
	@Inject
	private UrlService urlService;
	@Inject
	private SectionsController controller;
	@Inject
	private TreeRegistry treeRegistry;
	@Inject
	private CloudService cloudService;

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException
	{
		final CloudItemUrlParser parser = new CloudItemUrlParser();

		try
		{
			parser.parse(request);

			final String redirectUrl = parser.getRedirectUrl();
			if( redirectUrl != null )
			{
				response.sendRedirect(redirectUrl);
				return;
			}

			final Map<String, String[]> params = new HashMap<String, String[]>(request.getParameterMap());
			final SectionTree tree = treeRegistry.getTreeForPath("/cloud/viewitem.do");
			final URI uri = urlService.getUriForRequest(request, null);
			final URI baseUri = urlService.getBaseUriFromRequest(request);
			final String servletPath = '/' + baseUri.relativize(uri).getPath();

			final CloudItem item = cloudService.getItem(parser.getUuid(), parser.getVersion());
			if( item == null )
			{
				throw new NotFoundException(LABEL_ITEM_NOT_FOUND.getText());
			}
			final CloudViewableItem vitem = new CloudViewableItem(item, "summary".equals(parser.getToggle()));
			vitem.setFromRequest(true);
			final MutableSectionInfo info = controller.createInfo(tree, servletPath, request, response, null, params,
				Collections.singletonMap(CloudViewableItem.class, vitem));

			final RootCloudViewItemSection root = info.lookupSection(RootCloudViewItemSection.class);
			root.setAttachment(info, parser.getAttachmentUuid());

			controller.execute(info);
		}
		catch( Exception p )
		{
			SectionInfo info = controller.createInfo("/cloud/viewitem.do", request, response, null, null, null);
			controller.handleException(info, p, null);
		}
	}

	@NonNullByDefault(false)
	public class CloudItemUrlParser
	{
		private String uuid;
		private int version;
		private String toggle;
		private String attachmentUuid;
		private String redirectUrl;
		private String context;
		private HttpServletRequest request;
		private List<String> partList;
		private String originalUrl;

		public String getUuid()
		{
			return uuid;
		}

		public int getVersion()
		{
			return version;
		}

		public String getToggle()
		{
			return toggle;
		}

		public String getAttachmentUuid()
		{
			return attachmentUuid;
		}

		public String getRedirectUrl()
		{
			return redirectUrl;
		}

		public void parse(HttpServletRequest request) throws ParseException
		{
			this.request = request;
			this.originalUrl = request.getPathInfo();

			partList = new ArrayList<String>();
			for( String part : originalUrl.split("/") )
			{
				if( !Check.isEmpty(part) )
				{
					partList.add(part);
				}
			}

			setupContext();

			if( partList.size() < 2 )
			{
				throw new ParseException("Invalid URL missing UUID and/or version: " + originalUrl, 1);
			}

			uuid = partList.get(0);
			try
			{
				version = Integer.parseInt(partList.get(1));
			}
			catch( NumberFormatException nfe )
			{
				version = 0;
			}

			int attachStart = 2;
			// may contain "summary" or "attachment"
			if( partList.size() > 2 )
			{
				String p3 = partList.get(2);
				if( p3.equals("summary") || p3.equals("attachment") )
				{
					toggle = p3;
					attachStart = 3;
				}
			}

			if( partList.size() > attachStart )
			{
				attachmentUuid = partList.get(attachStart);
			}
			checkForRedirect();
		}

		protected void checkForRedirect()
		{
			if( version == 0 )
			{
				redirToLatest();
			}
		}

		protected void redirToLatest()
		{
			// get latest version from cloud
			final int version = cloudService.getLiveItemVersion(uuid);
			if( !(this.version == version) )
			{
				setupRedirectFromPath(context + uuid + '/' + version + '/' + (toggle == null ? "" : toggle + '/')
					+ (attachmentUuid == null ? "" : attachmentUuid + '/'));
			}
		}

		protected void setupRedirectFromPath(String path)
		{
			try
			{
				redirectUrl = new URL(institutionService.getInstitutionUrl(), URLUtils.urlEncode(path, false))
					.toString();
				String queryString = request.getQueryString();
				if( queryString != null )
				{
					redirectUrl += '?' + queryString;
				}
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}

		protected void setupContext()
		{
			context = request.getServletPath().substring(1) + '/';
		}

		public String getOriginalUrl()
		{
			return originalUrl;
		}
	}
}
