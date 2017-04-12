package com.tle.core.usermanagement.shibboleth.wrapper;

import hurl.build.QueryBuilder;
import hurl.build.UriBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.tle.beans.usermanagement.shibboleth.wrapper.ExternalAuthorisationWrapperSettings;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.user.UserService;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;
import com.tle.plugins.ump.UserManagementLogonFilter;
import com.tle.web.dispatcher.FilterResult;

/**
 * @author aholland
 */
@Bind
@SuppressWarnings("nls")
public class ExternalAuthorisationLogonFilter implements UserManagementLogonFilter
{
	private static final Logger LOGGER = Logger.getLogger(ExternalAuthorisationLogonFilter.class);
	private static final String PARAM_NO_AUTO_LOGIN = "NO_AUTO_LOGIN";

	@Inject
	private UserService userService;
	@Inject
	private ConfigurationService configurationService;

	private ExternalAuthorisationWrapperSettings settings;

	@Override
	public boolean init(Map<Object, Object> attributes)
	{
		settings = configurationService.getProperties(new ExternalAuthorisationWrapperSettings());
		if( settings.isEnabled() && LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("Usage type: " + settings.getUsageType());
			LOGGER.debug("HTTP Header: " + settings.getHttpHeaderName());
			LOGGER.debug("Env Var: " + settings.getEnvironmentVarName());
			LOGGER.debug("Logout URL: " + settings.getLogoutUrl());
		}
		return settings.isEnabled();
	}

	@Override
	public void addStateParameters(HttpServletRequest request, Map<String, String[]> params)
	{
		if( request.getParameter(PARAM_NO_AUTO_LOGIN) != null )
		{
			params.put(PARAM_NO_AUTO_LOGIN, new String[]{"true"});
		}
	}

	@Override
	public FilterResult filter(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		if( !CurrentUser.isGuest() || request.getParameter(PARAM_NO_AUTO_LOGIN) != null )
		{
			return null;
		}
		String username = null;
		if( settings.isRemoteUser() )
		{
			username = request.getRemoteUser();
		}
		else if( settings.isHTTPHeader() )
		{
			username = request.getHeader(settings.getHttpHeaderName());
		}
		else if( settings.isEnvironmentVar() )
		{
			username = (String) request.getAttribute(settings.getEnvironmentVarName());
		}

		if( username != null )
		{
			UserState userState = userService.authenticateAsUser(username,
				userService.getWebAuthenticationDetails(request));
			userService.login(userState, false);
			return FilterResult.FILTER_CONTINUE;
		}
		return null;
	}

	@Override
	public URI logoutRedirect(URI loggedOutURI)
	{
		String logoutUrl = settings.getLogoutUrl();
		if( !Check.isEmpty(logoutUrl) )
		{
			return URI.create(logoutUrl);
		}
		return null;
	}

	@Override
	public URI logoutURI(UserState state, URI loggedOutURI)
	{
		QueryBuilder loggedOutQB = QueryBuilder.create();
		String loggedOutQuery = loggedOutURI.getQuery();
		if( loggedOutQuery != null )
		{
			loggedOutQB.parse(loggedOutQuery);
		}
		loggedOutQB.addParam(PARAM_NO_AUTO_LOGIN, "true");
		UriBuilder builder = UriBuilder.create(loggedOutURI);
		builder.setQuery(loggedOutQB);
		return builder.build();
	}
}
