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

package com.tle.web.remoting.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.springframework.web.HttpRequestHandler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;

public abstract class AbstractRemoteHandler<T> extends HttpServlet
{
	private Map<String, Extension> mappings;
	private Map<Extension, HttpRequestHandler> handlerMappings = Collections
		.synchronizedMap(new HashMap<Extension, HttpRequestHandler>());
	protected PluginTracker<T> tracker;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		Map<String, Extension> locMappings = getMappings();
		String pathInfo = request.getPathInfo();
		if( pathInfo == null )
		{
			pathInfo = request.getServletPath();
		}
		Extension extension = locMappings.get(pathInfo);
		HttpRequestHandler handler;
		if( extension != null )
		{
			handler = handlerMappings.get(extension);
			if( handler == null )
			{
				synchronized( extension )
				{
					handler = handlerMappings.get(extension);
					if( handler == null )
					{
						handler = createHandler(extension);
						handlerMappings.put(extension, handler);
					}
				}
			}
			handler.handleRequest(request, response);
			return;
		}
		response.sendError(404);
	}

	protected HttpRequestHandler createHandler(Extension extension)
	{
		T handlerBean = tracker.getBeanByExtension(extension); // NOSONAR
		if( handlerBean instanceof HttpRequestHandler )
		{
			return (HttpRequestHandler) handlerBean;
		}
		return createHandlerFromBean(extension, handlerBean);
	}

	protected abstract HttpRequestHandler createHandlerFromBean(Extension extension, T handlerBean);

	private synchronized Map<String, Extension> getMappings()
	{
		if( mappings == null || tracker.needsUpdate() )
		{
			Builder<String, Extension> builder = new ImmutableMap.Builder<String, Extension>();
			List<Extension> extensions = tracker.getExtensions();
			for( Extension extension : extensions )
			{
				builder.put(getPathForExtension(extension), extension);
			}
			mappings = builder.build();
		}
		return mappings;
	}

	@SuppressWarnings("nls")
	private String getPathForExtension(Extension extension)
	{
		Parameter urlParam = extension.getParameter("url");
		if( urlParam != null )
		{
			return urlParam.valueAsString();
		}
		Parameter classParam = extension.getParameter("class");
		return "/" + classParam.valueAsString() + ".service";
	}

	@SuppressWarnings("nls")
	@Inject
	public void setPluginService(PluginService pluginService)
	{
		tracker = new PluginTracker<T>(pluginService, "com.tle.web.services", getExtensionPointName(), null).setBeanKey("bean");
	}

	protected abstract String getExtensionPointName();
}
