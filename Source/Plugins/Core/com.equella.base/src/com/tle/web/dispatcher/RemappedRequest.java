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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.tle.common.URLUtils;

public final class RemappedRequest extends HttpServletRequestWrapper
{
	private String contextPath;
	private String servletPath;
	private String pathInfo;
	private String requestURI;
	private final HttpServletRequest wrapped;

	private RemappedRequest(HttpServletRequest request, String context, String servletPath, String pathInfo)
	{
		super(request);
		this.wrapped = request;
		this.contextPath = context;
		this.servletPath = servletPath;
		this.pathInfo = pathInfo;
		setupURI();
	}

	@SuppressWarnings("nls")
	private void setupURI()
	{
		// Note that this will not give us an exact match for the original
		// Request URI since plus symbols will be instead be reconstructed as
		// %20's. Generating correct URLs isn't a bad thing though, right?
		this.requestURI = URLUtils.urlEncode(contextPath + servletPath + (pathInfo != null ? pathInfo : ""), false);
	}

	@Override
	public String getContextPath()
	{
		return contextPath;
	}

	@Override
	public String getPathInfo()
	{
		return pathInfo;
	}

	@Override
	public String getServletPath()
	{
		return servletPath;
	}

	@Override
	public String getRequestURI()
	{
		return requestURI;
	}

	@Override
	public void setAttribute(String name, Object o)
	{
		if( name.equals("org.apache.catalina.core.DISPATCHER_REQUEST_PATH") ) //$NON-NLS-1$
		{
			try
			{
				servletPath = URLDecoder.decode((String) o, "UTF-8"); //$NON-NLS-1$
			}
			catch( UnsupportedEncodingException e )
			{
				throw new RuntimeException();
			}
			pathInfo = null;
			setupURI();
		}
		super.setAttribute(name, o);
	}

	public HttpServletRequest getWrapped()
	{
		return wrapped;
	}

	public static HttpServletRequest wrap(HttpServletRequest request, String context, String servletPath,
		String pathInfo)
	{
		if( request instanceof RemappedRequest )
		{
			RemappedRequest orig = (RemappedRequest) request;
			orig.contextPath = context;
			orig.pathInfo = pathInfo;
			orig.servletPath = servletPath;
			orig.setupURI();
			return orig;
		}
		else
		{
			return new RemappedRequest(request, context, servletPath, pathInfo);
		}
	}
}
