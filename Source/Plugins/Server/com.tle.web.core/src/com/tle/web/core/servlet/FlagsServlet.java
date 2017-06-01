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

package com.tle.web.core.servlet;

import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.OperationNotSupportedException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ViewableItemType;
import com.tle.common.search.DefaultSearch;
import com.tle.core.filesystem.SystemFile;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.core.services.XsltService;
import com.tle.core.services.item.FreeTextService;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.services.item.ItemService;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
public class FlagsServlet extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(FlagsServlet.class);

	private static final String RSP_PARAM_QUERY = "q"; //$NON-NLS-1$
	private static final String RSP_PARAM_PAGE_SIZE = "ps"; //$NON-NLS-1$
	private static final String RSP_PARAM_MAX_RESULTS = "mr"; //$NON-NLS-1$
	private static final String RSP_PARAM_KEYWORD = "kc"; //$NON-NLS-1$
	private static final String RSP_PARAM_TOKEN = "token"; //$NON-NLS-1$
	private static final String RSP_PARAM_START_NUMBER = "start"; //$NON-NLS-1$

	private final Map<String, Integer> keywordMapping;
	private final Cache<String, FlagsSearchRequest> requestCache = CacheBuilder.newBuilder().softValues()
		.expireAfterAccess(1, TimeUnit.HOURS).build();

	@Inject
	private FreeTextService freeTextService;
	@Inject
	private XsltService xsltService;
	@Inject
	private UrlService urlService;
	@Inject
	private ItemService itemService;

	public FlagsServlet()
	{
		keywordMapping = new HashMap<String, Integer>();
		keywordMapping.put("all", FlagsSearchRequest.KEYWORD_ALL); //$NON-NLS-1$
		keywordMapping.put("any", FlagsSearchRequest.KEYWORD_ANY); //$NON-NLS-1$
		keywordMapping.put("phrase", FlagsSearchRequest.KEYWORD_PHRASE); //$NON-NLS-1$
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException
	{
		try
		{
			String path = req.getPathInfo();
			if( path.equals("/search") ) //$NON-NLS-1$
			{
				search(req, resp);
			}
			else
			{
				throw new OperationNotSupportedException("We don't like '" + path + '\'');
			}
		}
		catch( Exception e )
		{
			LOGGER.error("Error in Flags Servlet", e);
			throw new ServletException(e.getMessage());
		}
	}

	private void search(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		// Setup our search request
		FlagsSearchRequest searchReq = getSearchRequest(request);
		// Do the search
		FreetextSearchResults<FreetextResult> results = freeTextService.search(searchReq,
			searchReq.getStartNumber() - 1, searchReq.getPageSize());

		// Check the available results are within the required maxResults
		long available = results.getAvailable();
		available = Math.min(available, searchReq.getMaxResults());

		PropBagEx xml = new PropBagEx();
		xml = xml.newSubtree("results"); //$NON-NLS-1$

		xml.setNode("@count", results.getCount()); //$NON-NLS-1$
		xml.setNode("available", available); //$NON-NLS-1$
		xml.setNode("token", searchReq.getToken()); //$NON-NLS-1$

		for( Item item : results.getResults() )
		{
			PropBagEx result = xml.newSubtree("result"); //$NON-NLS-1$
			result.setNode("@id", item.getId()); //$NON-NLS-1$
			result.setNode("@url", getItemUrl(ViewableItemType.ITEMS, item.getItemId(), //$NON-NLS-1$
				"").toString()); //$NON-NLS-1$
			result.append("", itemService.getItemXmlPropBag(item)); //$NON-NLS-1$
		}

		// XSLT the results
		String output = xsltService.transform(new SystemFile(), "flags-results.xslt", xml, false); //$NON-NLS-1$

		// Return the generated output
		response.setContentType("text/xml"); //$NON-NLS-1$
		Writer writer = response.getWriter();
		writer.write(output);
		writer.flush();
	}

	private FlagsSearchRequest getSearchRequest(HttpServletRequest request)
	{
		FlagsSearchRequest bean = null;

		// Check if they want an existing token
		String token = request.getParameter(RSP_PARAM_TOKEN);
		if( token != null )
		{
			bean = requestCache.getIfPresent(token);
			if( bean == null )
			{
				throw new IllegalArgumentException("Token " + token + " does not exist, or has expired");
			}
		}
		else
		{
			bean = new FlagsSearchRequest();
			requestCache.put(bean.getToken(), bean);
		}

		setQuery(request, bean);
		setPageSize(request, bean);
		setMaxResults(request, bean);
		setStartNumber(request, bean);
		setKeywordConstraint(request, bean);

		return bean;
	}

	private void setPageSize(HttpServletRequest request, FlagsSearchRequest bean)
	{
		int value = getIntParam(request, RSP_PARAM_PAGE_SIZE);
		if( value >= 0 )
		{
			bean.setPageSize(value);
		}
	}

	private void setStartNumber(HttpServletRequest request, FlagsSearchRequest bean)
	{
		int value = getIntParam(request, RSP_PARAM_START_NUMBER);
		if( value >= 0 )
		{
			bean.setStartNumber(value);
		}
	}

	private void setMaxResults(HttpServletRequest request, FlagsSearchRequest bean)
	{
		int value = getIntParam(request, RSP_PARAM_MAX_RESULTS);
		if( value >= 0 )
		{
			bean.setMaxResults(value);
		}
	}

	private void setQuery(HttpServletRequest request, FlagsSearchRequest bean)
	{
		String value = request.getParameter(RSP_PARAM_QUERY);
		if( value != null )
		{
			bean.setQuery(value);
		}
	}

	private void setKeywordConstraint(HttpServletRequest request, FlagsSearchRequest bean)
	{
		String value = request.getParameter(RSP_PARAM_KEYWORD);
		if( value != null )
		{
			Integer keyword = keywordMapping.get(value);
			if( keyword != null )
			{
				bean.setKeywordConstraint(keyword.intValue());
			}
		}
	}

	/**
	 * Returns a negative value if parameter does not exist.
	 */
	private int getIntParam(HttpServletRequest request, String name) throws IllegalArgumentException
	{
		int value = -1;

		String param = request.getParameter(name);
		if( param != null )
		{
			try
			{
				value = Integer.valueOf(param).intValue();
			}
			catch( NumberFormatException ex )
			{
				throw new IllegalArgumentException("Parameter " + param + " must be an integer");
			}
		}

		return value;
	}

	private static class FlagsSearchRequest extends DefaultSearch
	{
		private static final long serialVersionUID = 1L;
		public static final int KEYWORD_ALL = 0;
		public static final int KEYWORD_ANY = 1;
		public static final int KEYWORD_PHRASE = 2;

		private static final long TOKEN_BASE = 100000000000000000L;

		private static final int DEFAULT_PAGE_SIZE = 10;
		private static final int DEFAULT_START_NUMBER = 1;
		private static final int DEFAULT_MAX_RESULTS = 100;
		private static final int DEFAULT_KEYWORD = KEYWORD_ALL;

		private static final int MIN_PAGE_SIZE = 10;
		private static final int MIN_START_NUMBER = 1;
		private static final int MIN_MAX_RESULTS = 50;

		private static final int MAX_PAGE_SIZE = 100;
		private static final int MAX_START_NUMBER = Integer.MAX_VALUE;
		private static final int MAX_MAX_RESULTS = 200;

		private String token;
		private int pageSize;
		private int startNumber;
		private int maxResults;
		private int keywordConstraint;

		/**
		 * Constructs a new Flags Search Request.
		 */
		public FlagsSearchRequest()
		{
			super();

			// Set the defaults
			setPageSize(DEFAULT_PAGE_SIZE);
			setStartNumber(DEFAULT_START_NUMBER);
			setMaxResults(DEFAULT_MAX_RESULTS);
			setKeywordConstraint(DEFAULT_KEYWORD);
			setToken(generateToken());
		}

		@Override
		public String getQuery()
		{
			String query = super.getQuery();
			if( keywordConstraint == KEYWORD_PHRASE )
			{
				query = "\"" + query + "\"";
			}
			else
			{
				String operator;
				if( keywordConstraint == KEYWORD_ALL )
				{
					operator = " AND ";
				}
				else
				{
					operator = " OR ";
				}
				query = query.replaceAll("\\s+", operator);
			}
			return query;
		}

		/**
		 * @param keywordConstraint The keywordConstraint to set.
		 */
		public void setKeywordConstraint(int keywordConstraint)
		{
			if( keywordConstraint != KEYWORD_ALL && keywordConstraint != KEYWORD_ANY
				&& keywordConstraint != KEYWORD_PHRASE )
			{
				throw new IllegalArgumentException(
					"Parameter must be one of KEYWORD_ALL, KEYWORD_ANY, or KEYWORD_PHRASE");
			}
			this.keywordConstraint = keywordConstraint;
		}

		/**
		 * @return Returns the maxResults.
		 */
		public int getMaxResults()
		{
			return maxResults;
		}

		/**
		 * @param maxResults The maxResults to set.
		 */
		public void setMaxResults(int maxResults)
		{
			checkRange("Max results", maxResults, MIN_MAX_RESULTS, MAX_MAX_RESULTS);
			this.maxResults = maxResults;
		}

		/**
		 * @return Returns the pageSize.
		 */
		public int getPageSize()
		{
			return pageSize;
		}

		/**
		 * @param pageSize The pageSize to set.
		 */
		public void setPageSize(int pageSize)
		{
			checkRange("Page size", pageSize, MIN_PAGE_SIZE, MAX_PAGE_SIZE);
			this.pageSize = pageSize;
		}

		/**
		 * @return Returns the startNumber.
		 */
		public int getStartNumber()
		{
			return startNumber;
		}

		/**
		 * @param startNumber The startNumber to set.
		 */
		public void setStartNumber(int startNumber)
		{
			checkRange("Start number", startNumber, MIN_START_NUMBER, MAX_START_NUMBER);
			this.startNumber = startNumber;
		}

		/**
		 * @return Returns the token.
		 */
		public String getToken()
		{
			return token;
		}

		/**
		 * @param token The token to set.
		 */
		private void setToken(String token)
		{
			this.token = token;
		}

		private void checkRange(String name, int value, int min, int max)
		{
			if( min > value || value > max )
			{
				throw new IllegalArgumentException(name + " must be between " + min + " and " + max + " inclusive");
			}
		}

		private String generateToken()
		{
			// Start with something 18 digits long
			long value = TOKEN_BASE;
			value += Math.random() * value;
			return Long.toString(value);
		}
	}

	public URL getItemUrl(ViewableItemType viewable, ItemId itemid, String postfix)
	{
		if( viewable == null )
		{
			viewable = ViewableItemType.ITEMS;
		}
		return getItemUrl(itemid, viewable.getContext(), postfix);
	}

	private URL getItemUrl(ItemId itemid, String context, String postfix)
	{
		URL firstAppend = null;
		try
		{
			firstAppend = append(urlService.getInstitutionUrl(), context + '/' + itemid.toString());
			return new URL(firstAppend, postfix);
		}
		catch( MalformedURLException ex )
		{
			throw malformed(ex, firstAppend, postfix);
		}
	}

	/**
	 * Assumes given URL ends with a forward slash, and always returns a URL
	 * ending with a forward slash.
	 */
	private URL append(URL url, String extra)
	{
		String file = url.getFile() + extra;
		if( !file.endsWith("/") )
		{
			file += '/';
		}

		try
		{
			return new URL(url, file);
		}
		catch( MalformedURLException ex )
		{
			throw malformed(ex, url, file);
		}
	}

	protected RuntimeException malformed(Throwable ex, Object... bits)
	{
		StringBuilder msg = new StringBuilder("Error creating URL");
		for( Object bit : bits )
		{
			if( bit != null )
			{
				msg.append(", ");
				msg.append(bit.toString());
			}
		}

		return new RuntimeException(msg.toString(), ex);
	}

}
