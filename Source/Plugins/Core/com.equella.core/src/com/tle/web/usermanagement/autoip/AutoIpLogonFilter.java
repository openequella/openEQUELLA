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

package com.tle.web.usermanagement.autoip;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.guice.Bind;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.common.settings.standard.AutoLogin;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.plugins.ump.UserManagementLogonFilter;
import com.tle.web.dispatcher.FilterResult;

import hurl.build.QueryBuilder;
import hurl.build.UriBuilder;

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
