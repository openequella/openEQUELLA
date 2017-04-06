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
}
