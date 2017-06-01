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

package com.tle.integration.lti.canvasextension;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.tle.core.guice.Bind;
import com.tle.web.lti.usermanagement.LtiWrapperExtension;

/**
 * Checks for Canvas custom_canvas_user_login_id param to match an existing
 * user.
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class CanvasLtiWrapperExtension implements LtiWrapperExtension
{
	@Override
	public String getUserId(HttpServletRequest request)
	{
		return request.getParameter("custom_canvas_user_id");
	}

	@Override
	public String getUsername(HttpServletRequest request)
	{
		return request.getParameter("custom_canvas_user_login_id");
	}

	@Override
	public String getFirstName(HttpServletRequest request)
	{
		return null;
	}

	@Override
	public String getLastName(HttpServletRequest request)
	{
		return null;
	}

	@Override
	public String getEmail(HttpServletRequest request)
	{
		return null;
	}

	@Override
	public boolean isPrefixUserId()
	{
		return true;
	}
}
