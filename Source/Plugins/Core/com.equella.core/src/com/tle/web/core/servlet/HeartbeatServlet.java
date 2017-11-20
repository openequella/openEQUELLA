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

import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;

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
