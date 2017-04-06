package com.tle.web.oauth.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dytech.edge.exceptions.WebException;
import com.tle.beans.system.AutoLogin;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.web.dispatcher.AbstractWebFilter;
import com.tle.web.dispatcher.FilterResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

/**
 * Mapped to /oauth/*
 * 
 * @author Aaron
 */
@Bind
@Singleton
public class OAuthFilter extends AbstractWebFilter
{
	@Inject
	private UserService userService;
	@PlugKey("oauth.filter.error.mustbehttps")
	private static Label LABEL_ERROR;

	@SuppressWarnings("nls")
	@Override
	public FilterResult filterRequest(HttpServletRequest request, HttpServletResponse response) throws IOException,
		ServletException
	{
		AutoLogin autoLogin = userService.getAttribute(AutoLogin.class);
		if( autoLogin != null && !request.isSecure() && autoLogin.isLoginViaSSL() )
		{
			throw new WebException(400, "ssl", LABEL_ERROR.getText());
		}
		return FilterResult.FILTER_CONTINUE;
	}
}
