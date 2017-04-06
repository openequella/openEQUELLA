package com.tle.web.raw.servlet;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import com.tle.common.PathUtils;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.web.resources.AbstractResourcesServlet;

@Bind
@Singleton
@SuppressWarnings("nls")
public class FallbackServlet extends AbstractResourcesServlet
{
	private static final Map<String, String> CONTENT_TYPES = ImmutableMap.of("js", "application/javascript", "css",
		"text/css", "gif", "image/gif", "png", "image/png");

	@Inject
	private PluginService pluginService;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		String pathInfo = request.getPathInfo();
		String mofo = request.getServletPath() + Strings.nullToEmpty(pathInfo);
		String mimeType = null;

		if( !Strings.isNullOrEmpty(pathInfo) )
		{
			String ext = PathUtils.extension(pathInfo);
			if( ext != null && CONTENT_TYPES.containsKey(ext) )
			{
				mimeType = CONTENT_TYPES.get(ext);
			}
		}
		service(request, response, mofo, mimeType);
	}

	@Override
	public String getRootPath()
	{
		return "web/";
	}

	@Override
	public String getPluginId(HttpServletRequest request)
	{
		return pluginService.getPluginIdForObject(FallbackServlet.class);
	}
}
