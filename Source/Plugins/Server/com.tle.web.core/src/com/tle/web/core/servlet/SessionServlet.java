package com.tle.web.core.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.common.usermanagement.user.WebAuthenticationDetails;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.exceptions.AuthenticationException;

@Bind
@Singleton
public class SessionServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	@Inject
	private UserService userService;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String token = req.getParameter("token");
		WebAuthenticationDetails details = userService.getWebAuthenticationDetails(req);
		try
		{
			if( token != null )
			{
				userService.loginWithToken(token, details, true);
			}
			else
			{
				String username = req.getParameter("username");
				String password = req.getParameter("password");
				if( username != null )
				{
					userService.login(username, password, details, true);
				}
				else
				{
					resp.sendError(400);
					return;
				}
			}
			resp.setStatus(200);
		}
		catch( AuthenticationException ae )
		{
			resp.sendError(401);
		}
	}
}
