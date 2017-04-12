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

import com.dytech.edge.exceptions.NotFoundException;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.core.services.item.ItemService;
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
			if( redirectUrl != null /*&& !"POST".equals(request.getMethod())*/ )
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
				redirectUrl = new URL(urlService.getInstitutionUrl(), URLUtils.urlEncode(path, false)).toString();
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
