package com.tle.integration.blackboard;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.web.resources.AbstractResourcesServlet;

@Bind
@Singleton
@SuppressWarnings("nls")
public class BlackboardIconServlet extends AbstractResourcesServlet
{
	@Inject
	private PluginService pluginService;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		service(request, response, request.getPathInfo(), null);
	}

	@Override
	public String getRootPath()
	{
		return "icons/";
	}

	@Override
	public String getPluginId(HttpServletRequest request)
	{
		return pluginService.getPluginIdForObject(BlackboardIconServlet.class);
	}
}
