package com.tle.web.usermanagement.autoip;

import hurl.build.QueryBuilder;
import hurl.build.UriBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.beans.system.AutoLogin;
import com.tle.core.guice.Bind;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;
import com.tle.plugins.ump.UserManagementLogonFilter;
import com.tle.web.dispatcher.FilterResult;

@Bind
public class AutoIpLogonFilter implements UserManagementLogonFilter
{
	private static final String PARAM_NOIP = "NO_IP_LOGIN"; //$NON-NLS-1$
	@Inject
	private ConfigurationService configService;
	@Inject
	private AutoIpLogonService autoIpLogonService;

	@Override
	public boolean init(Map<Object, Object> attributes)
	{
		AutoLogin settings = configService.getProperties(new AutoLogin());
		attributes.put(AutoLogin.class, settings);
		return settings.isEnabledViaIp() && !settings.isNotAutomatic();
	}

	@Override
	public FilterResult filter(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		if( !CurrentUser.isGuest() )
		{
			return null;
		}
		if( request.getParameter(PARAM_NOIP) == null && autoIpLogonService.autoLogon(request) )
		{
			return FilterResult.FILTER_CONTINUE;
		}
		return null;
	}

	@Override
	public URI logoutRedirect(URI loggedOutURI)
	{
		return null;
	}

	@Override
	public URI logoutURI(UserState state, URI loggedOutURI)
	{
		UriBuilder uri = UriBuilder.create(loggedOutURI);
		QueryBuilder qb = QueryBuilder.create();
		if( loggedOutURI.getQuery() != null )
		{
			qb.parse(loggedOutURI.getQuery());
		}
		qb.addParam(PARAM_NOIP, "true"); //$NON-NLS-1$
		uri.setQuery(qb);
		return uri.build();
	}

	@Override
	public void addStateParameters(HttpServletRequest request, Map<String, String[]> params)
	{
		if( request.getParameter(PARAM_NOIP) != null )
		{
			params.put(PARAM_NOIP, new String[]{"true"}); //$NON-NLS-1$
		}
	}

}
