package com.tle.web.mobile;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.web.resources.ResourcesService;

@SuppressWarnings("nls")
@Bind
@Singleton
public class MobileResourcesServlet extends HttpServlet
{
	private static String URL = ResourcesService.getResourceHelper(MobileResourcesServlet.class).url("mobilev38.html");
	private static final long serialVersionUID = 1L;

	@Inject
	private UrlService urlService;
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		response.sendRedirect(urlService.institutionalise(URL));
	}
}
