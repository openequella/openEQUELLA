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
