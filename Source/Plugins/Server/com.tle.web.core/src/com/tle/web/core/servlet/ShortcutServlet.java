/*
 * Created on 14/03/2006
 */
package com.tle.web.core.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dytech.edge.exceptions.NotFoundException;
import com.tle.beans.system.ShortcutUrls;
import com.tle.core.guice.Bind;
import com.tle.core.services.config.ConfigurationService;

@Bind
@Singleton
public class ShortcutServlet extends HttpServlet
{
	@Inject
	private ConfigurationService configService;

	public ShortcutServlet()
	{
		super();
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		String shortcut = request.getPathInfo().substring(1);
		String url = configService.getProperties(new ShortcutUrls()).getShortcuts().get(shortcut);
		if( url == null )
		{
			throw new NotFoundException("Shortcut '" + shortcut + "' does not exist", true);
		}
		response.sendRedirect(url);
	}
}
