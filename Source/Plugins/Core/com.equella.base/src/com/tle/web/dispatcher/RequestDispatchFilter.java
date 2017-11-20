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

package com.tle.web.dispatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;

public class RequestDispatchFilter implements Filter
{
	private static final Logger LOGGER = Logger.getLogger(RequestDispatchFilter.class);

	private static final String CALLBACKS_KEY = "com.tle.web.core.filtercallbacks";
	private PluginTracker<WebFilter> filterTracker;
	private ServletDispatcher servletDispatcher;
	private Map<Extension, WebPathMatcher> mappings;
	private String dispatcher;

	@Override
	public void destroy()
	{
		// nothing
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
		ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		Map<Extension, WebPathMatcher> locMappings = ensureMappings();
		try
		{
			for( Map.Entry<Extension, WebPathMatcher> entry : locMappings.entrySet() )
			{
				if( entry.getValue().matches(httpRequest.getServletPath()) )
				{
					WebFilter webFilter = filterTracker.getBeanByExtension(entry.getKey());
					FilterResult result = webFilter.filterRequest(httpRequest, httpResponse);
					if( result.isStop() )
					{
						return;
					}
					WebFilterCallback callback = result.getCallback();
					if( callback != null )
					{
						addCallback(httpRequest, callback);
					}
					if( httpResponse.isCommitted() )
					{
						return;
					}
					if( result.getFilteredRequest() != null )
					{
						httpRequest = result.getFilteredRequest();
					}
					if( result.getFilteredResponse() != null )
					{
						httpResponse = result.getFilteredResponse();
					}
					if( result.getForwardUrl() != null )
					{
						httpRequest.getRequestDispatcher(result.getForwardUrl()).forward(httpRequest, httpResponse);
						return;
					}
				}
			}
			FilterResult result = servletDispatcher.dispatch(httpRequest, httpResponse);
			if( result == null )
			{
				chain.doFilter(httpRequest, httpResponse);
			}
			else
			{
				if( result.getFilteredRequest() != null )
				{
					httpRequest = result.getFilteredRequest();
				}
				if( result.getFilteredResponse() != null )
				{
					httpResponse = result.getFilteredResponse();
				}
			}
		}
		finally
		{
			runCallbacks(httpRequest, httpResponse);
		}
	}

	@SuppressWarnings("unchecked")
	private void runCallbacks(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
	{
		List<WebFilterCallback> callbacks = (List<WebFilterCallback>) httpRequest.getAttribute(CALLBACKS_KEY);
		httpRequest.removeAttribute(CALLBACKS_KEY);
		if( callbacks != null )
		{
			// Run them in reverse, don't let one callback stop the rest from happening
			for( int i = callbacks.size() - 1; i >= 0; i-- )
			{
				try
				{
					callbacks.get(i).afterServlet(httpRequest, httpResponse);
				}
				catch( Exception e )
				{
					LOGGER.error("Error running callback", e);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void addCallback(HttpServletRequest httpRequest, WebFilterCallback callback)
	{
		List<WebFilterCallback> callbacks = (List<WebFilterCallback>) httpRequest.getAttribute(CALLBACKS_KEY);
		if( callbacks == null )
		{
			callbacks = new ArrayList<WebFilterCallback>();
			httpRequest.setAttribute(CALLBACKS_KEY, callbacks);
		}
		callbacks.add(callback);
	}

	private synchronized Map<Extension, WebPathMatcher> ensureMappings()
	{
		if( mappings == null && filterTracker.needsUpdate() )
		{
			mappings = new LinkedHashMap<Extension, WebPathMatcher>();
			List<Extension> exts = filterTracker.getExtensions();
			for( Extension ext : exts )
			{
				Collection<Parameter> dispatchers = ext.getParameters("dispatcher"); //$NON-NLS-1$
				boolean rightDispatcher = dispatchers.isEmpty();
				for( Parameter dispatch : dispatchers )
				{
					if( dispatcher.equals(dispatch.valueAsString()) )
					{
						rightDispatcher = true;
						break;
					}
				}
				if( rightDispatcher )
				{
					WebPathMatcher matcher = new WebPathMatcher();
					Collection<Parameter> params = ext.getParameters("url-pattern"); //$NON-NLS-1$
					for( Parameter pattern : params )
					{
						matcher.addPath(pattern.valueAsString());
					}
					Collection<Parameter> exclusions = ext.getParameters("exclude"); //$NON-NLS-1$
					for( Parameter path : exclusions )
					{
						matcher.addExclusion(path.valueAsString());
					}
					mappings.put(ext, matcher);
				}
			}
		}
		return mappings;
	}

	@SuppressWarnings("nls")
	@Override
	public void init(FilterConfig config) throws ServletException
	{
		PluginService pluginService = AbstractPluginService.get();
		filterTracker = new PluginTracker<WebFilter>(pluginService, "com.tle.web.core", "webFilter", null,
			new PluginTracker.ExtensionParamComparator("order")).setBeanKey("bean");
		servletDispatcher = ServletDispatcher.get(config.getServletContext(), pluginService);
		dispatcher = config.getInitParameter("dispatcher");
	}

}
