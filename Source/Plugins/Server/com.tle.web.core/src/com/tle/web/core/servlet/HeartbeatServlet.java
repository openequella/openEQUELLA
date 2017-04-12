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

import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;

@Bind
@Singleton
public class HeartbeatServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	@Inject
	private UserService userService;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		if( !CurrentUser.isGuest() && CurrentInstitution.get() != null )
		{
			userService.keepAlive();
		}

		// IE will cache this otherwise
		response.setHeader("Cache-Control", "no-cache"); //$NON-NLS-1$//$NON-NLS-2$
		response.setStatus(HttpServletResponse.SC_OK);
		// FF3 will try to parse as XML if no content type sent
		response.setContentType("text/plain"); //$NON-NLS-1$
		response.getOutputStream().close();
	}
}
