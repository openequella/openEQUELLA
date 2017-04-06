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

	private RemappedRequest(HttpServletRequest request, String context, String servletPath, String pathInfo)
	{
		super(request);
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
