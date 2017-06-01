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
import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.google.common.collect.Maps;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.dispatcher.MultiMatcher.ResolvedServlet;

public class ServletDispatcher
{
	private static final String DISPATCHER_KEY = "ServletDispatcher"; //$NON-NLS-1$
	private final PluginTracker<HttpServlet> servletTracker;
	private final Set<Extension> initedServlets = Collections.synchronizedSet(new HashSet<Extension>());
	private MultiMatcher multiMatcher;
	private final ServletContext context;

	@SuppressWarnings("nls")
	private ServletDispatcher(ServletContext servletContext, PluginService pluginService)
	{
		this.context = servletContext;
		servletTracker = new PluginTracker<HttpServlet>(pluginService, "com.tle.web.core", "webServlet", null)
			.setBeanKey("bean");
	}

	public static synchronized ServletDispatcher get(ServletContext servletContext, PluginService pluginService)
	{
		ServletDispatcher dispatcher = (ServletDispatcher) servletContext.getAttribute(DISPATCHER_KEY);

		if( dispatcher == null )
		{
			dispatcher = new ServletDispatcher(servletContext, pluginService);
			servletContext.setAttribute(DISPATCHER_KEY, dispatcher);
		}
		return dispatcher;
	}

	@SuppressWarnings("nls")
	public FilterResult dispatch(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
		throws ServletException, IOException
	{
		String servletPath = httpRequest.getServletPath();
		MultiMatcher mappings = ensureMappings();
		ResolvedServlet resolved = mappings.matchPath(servletPath);
		if( resolved != null )
		{
			Extension ext = resolved.getExtension();
			HttpServlet servlet;
			synchronized( ext )
			{
				servlet = servletTracker.getBeanByExtension(ext);
				if( !initedServlets.contains(ext) )
				{
					initedServlets.add(ext);
					Collection<Parameter> initParams = ext.getParameters("init-param");
					servlet.init(new DummyServletConfig(context, initParams, servlet.getClass().getName()));
				}
			}

			HttpServletRequest wrappedRequest = RemappedRequest.wrap(httpRequest, httpRequest.getContextPath(),
				resolved.getServletPath(), resolved.getPathInfo());
			try
			{
				servlet.service(wrappedRequest, httpResponse);
				httpResponse.flushBuffer();
			}
			catch( SocketException se )
			{
				// What are you going to do?
			}
			return new FilterResult(wrappedRequest);

		}
		return null;
	}

	private synchronized MultiMatcher ensureMappings()
	{
		if( multiMatcher == null && servletTracker.needsUpdate() )
		{
			multiMatcher = new MultiMatcher();
			List<Extension> exts = servletTracker.getExtensions();
			for( Extension ext : exts )
			{
				WebPathMatcher matcher = new WebPathMatcher();
				Collection<Parameter> params = ext.getParameters("url-pattern"); //$NON-NLS-1$
				for( Parameter pattern : params )
				{
					matcher.addPath(pattern.valueAsString());
				}
				multiMatcher.addMatcher(matcher, ext);
			}
		}
		return multiMatcher;
	}

	public static class DummyServletConfig implements ServletConfig
	{
		private final ServletContext context;
		private final String name;
		private final Map<String, String> initParams = Maps.newHashMap();

		@SuppressWarnings("nls")
		public DummyServletConfig(ServletContext context, Collection<Parameter> initParams, String name)
		{
			this.context = context;
			this.name = name;
			for( Parameter parameter : initParams )
			{
				this.initParams.put(parameter.getSubParameter("name").valueAsString(),
					parameter.getSubParameter("value").valueAsString());
			}
		}

		@Override
		public String getInitParameter(String name)
		{
			return initParams.get(name);
		}

		@Override
		public Enumeration<String> getInitParameterNames()
		{
			return Collections.enumeration(initParams.keySet());
		}

		@Override
		public ServletContext getServletContext()
		{
			return context;
		}

		@Override
		public String getServletName()
		{
			return name;
		}
	}

}
