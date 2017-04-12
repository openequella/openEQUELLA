package com.tle.web.core.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.web.dispatcher.FilterResult;

@Bind
public class LogonFilter extends OncePerRequestFilter
{
	@Inject
	private UserService userService;

	@Override
	protected FilterResult doFilterInternal(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		return userService.runLogonFilters(request, response);
	}

}
