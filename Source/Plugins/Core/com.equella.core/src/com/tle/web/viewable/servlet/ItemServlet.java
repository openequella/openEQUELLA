/*
 * Copyright 2019 Apereo
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

package com.tle.web.viewable.servlet;

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

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.service.ItemService;
import com.tle.core.services.UrlService;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.registry.TreeRegistry;
import com.tle.web.viewable.NewDefaultViewableItem;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;

@Bind
@Singleton
public class ItemServlet extends HttpServlet
{
	public static final String VIEWABLE_ITEM = "viewableitem"; //$NON-NLS-1$

	@Inject
	private InstitutionService institutionService;
	@Inject
	private UrlService urlService;
	@Inject
	private ItemService itemService;
	@Inject
	private ViewableItemFactory viewableItemFactory;
	@Inject
	private SectionsController controller;
	@Inject
	private TreeRegistry treeRegistry;

	@SuppressWarnings("nls")
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException
	{
		final ItemUrlParser parser = getItemUrlParser();

		Throwable t;
		try
		{
			parser.parse(request);

			String redirectUrl = parser.getRedirectUrl();
			// Redirect only appropriate if original request not a POST.
			if( redirectUrl != null /* && !"POST".equals(request.getMethod()) */ )
			{
				response.sendRedirect(redirectUrl);
				return;
			}

			Map<String, String[]> params = new HashMap<String, String[]>(request.getParameterMap());
			params.put("filename", new String[]{parser.getPath()});
			SectionTree tree = treeRegistry.getTreeForPath("/viewitem/viewitem.do");
			URI uri = urlService.getUriForRequest(request, null);
			// NB: the URI (& servletPath) will preserve the original item
			// version, and if that was a 0, it will differ from the updated
			// ItemId. This anomaly is considered harmless & not worth
			// adjusting. The parser preserves the ItemId.
			URI baseUri = urlService.getBaseUriFromRequest(request);

			//EQ-2696 SSO fixes Start			
			Institution institution = CurrentInstitution.get();
			URI institutionURI = institution.getUrlAsUri();

			if( institutionURI != null && institutionURI.getScheme().length() > 0
				&& institutionURI.getScheme().equalsIgnoreCase("http") )
			{
				String set_cookie = response.getHeader("Set-Cookie");
				if( set_cookie != null && set_cookie.length() > 0 && set_cookie.contains("Secure") )
				{
					response.setHeader("Set-Cookie", set_cookie.replace("Secure", ""));
				}
			}
			//EQ-2696 SSO fixes End

			String servletPath = '/' + baseUri.relativize(uri).getPath();
			ViewableItem<?> viewableItem = parser.createViewableItem();
			MutableSectionInfo info = controller.createInfo(tree, servletPath, request, response, null, params,
				Collections.singletonMap(VIEWABLE_ITEM, viewableItem));
			controller.execute(info);
			return;
		}
		catch( ParseException pe )
		{
			RuntimeApplicationException rt = new RuntimeApplicationException(pe);
			rt.setShowStackTrace(false);
			t = rt;
		}
		catch( NumberFormatException ne )
		{
			throw new NotFoundException(true);
		}
		catch( Exception p )
		{
			t = p;
		}
		SectionInfo info = controller.createInfo("/viewitem/viewitem.do", request, response, null, null, null);
		controller.handleException(info, t, null);
	}

	protected ItemUrlParser getItemUrlParser()
	{
		return new NewItemUrlParser();
	}

	public interface ItemUrlParser
	{
		void parse(HttpServletRequest request) throws ParseException;

		ViewableItem<?> createViewableItem();

		String getPath();

		String getRedirectUrl();

		String getOriginalUrl();
	}

	public class NewItemUrlParser implements ItemUrlParser
	{
		protected ItemKey itemId;
		private String path;
		private String redirectUrl;
		protected String context;
		protected HttpServletRequest request;
		protected List<String> partList;
		protected String originalUrl;

		@Override
		public String getPath()
		{
			return path;
		}

		@Override
		public String getRedirectUrl()
		{
			return redirectUrl;
		}

		@Override
		public ViewableItem<?> createViewableItem()
		{
			NewDefaultViewableItem newItem = viewableItemFactory.createNewViewableItem(itemId);
			newItem.setFromRequest(true);
			return newItem;
		}

		@SuppressWarnings("nls")
		@Override
		public void parse(HttpServletRequest request) throws ParseException, NotFoundException
		{
			try
			{
				this.request = request;
				this.originalUrl = request.getPathInfo();

				final boolean endSlash = originalUrl.endsWith("/");

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

				String id = partList.get(0);
				String version = partList.get(1);
				StringBuilder itemPath = new StringBuilder();
				for( int i = 2; i < partList.size(); i++ )
				{
					if( i > 2 )
					{
						itemPath.append('/');
					}
					itemPath.append(partList.get(i));
				}

				String fullid = id + '/' + version;
				if( partList.size() == 2 && !endSlash )
				{
					setupRedirectFromPath(context + fullid + '/');
					return;
				}

				if( partList.size() > 2 && endSlash )
				{
					itemPath.append('/');
				}

				this.path = itemPath.toString();
				itemId = ItemTaskId.parse(fullid);
				if( itemId.getVersion() == -1 )
				{
					throw new NotFoundException(originalUrl, true);
				}
				else
				{
					checkForRedirect();
				}
			}
			catch( NumberFormatException ne )
			{
				throw new NotFoundException(true);
			}
		}

		protected void checkForRedirect()
		{
			if( itemId.getVersion() == 0 )
			{
				redirToLatest();
			}
		}

		protected void redirToLatest()
		{
			int version = itemService.getLiveItemVersion(itemId.getUuid());
			if( !(itemId.getVersion() == version) )
			{
				// in the event of a POST, we will persist with the existing
				// request, and hence will refer to the ItemId again as the
				// request is processed.
				itemId = new ItemId(itemId.getUuid(), version);
				setupRedirectFromPath(context + itemId.toString(version) + '/' + path);
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

		@Override
		public String getOriginalUrl()
		{
			return originalUrl;
		}
	}
}
