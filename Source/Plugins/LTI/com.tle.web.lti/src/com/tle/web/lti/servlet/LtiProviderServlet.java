package com.tle.web.lti.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dytech.edge.exceptions.WebException;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.web.sections.equella.annotation.PlugKey;

@Bind
@Singleton
@SuppressWarnings("nls")
public class LtiProviderServlet extends HttpServlet
{
	@PlugKey("redirect.lti.missing.param.")
	private static String ERROR_PREFIX;

	@Inject
	private UrlService urlService;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String customParam = req.getParameter("custom_equella_url");
		if( !Check.isEmpty(customParam) )
		{
			customParam = customParam.startsWith("/") ? customParam.substring(1) : customParam;
			resp.sendRedirect(urlService.institutionalise(customParam));
		}
		else
		{
			// HTTP 400 Bad request - missing parameter
			throw new WebException(400, CurrentLocale.get(ERROR_PREFIX + "error"), CurrentLocale.get(ERROR_PREFIX
				+ "message"));
		}
	}
}
