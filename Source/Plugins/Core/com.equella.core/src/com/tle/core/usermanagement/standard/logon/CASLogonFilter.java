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

package com.tle.core.usermanagement.standard.logon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.tle.beans.usermanagement.standard.wrapper.CASConfiguration;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.util.BlindSSLSocketFactory;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.core.services.user.UserService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.plugins.ump.UserManagementLogonFilter;
import com.tle.web.dispatcher.FilterResult;

import hurl.build.QueryBuilder;
import hurl.build.UriBuilder;
import hurl.parts.Query;

@Bind
@SuppressWarnings("nls")
public class CASLogonFilter implements UserManagementLogonFilter
{

	private static final Logger LOGGER = Logger.getLogger(CASLogonFilter.class);

	private static final String TICKET_VALIDATE_SUCCESS = "yes";
	private static final String PARAM_TICKET = "ticket";
	private static final String PARAM_SERVICE = "service";
	private static final String PARAM_LOGOUT_URL = "url";
	private static final String PARAM_NOCAS = "NO_AUTO_LOGIN";

	private static final URI URI_LOGIN = URI.create("login");
	private static final URI URI_LOGOUT = URI.create("logout");
	private static final URI URI_VALIDATE = URI.create("validate");

	private URI casLoginUri;
	private URI casValidateUri;
	private URI casLogoutURI;

	@Inject
	private UrlService urlService;
	@Inject
	private UserService userService;
	@Inject
	private ConfigurationService configurationService;

	@Override
	public boolean init(Map<Object, Object> attributes)
	{
		CASConfiguration config = configurationService.getProperties(new CASConfiguration());
		if( config.isEnabled() )
		{
			try
			{
				URI casURI = config.getUrl().toURI();
				casLogoutURI = config.getLogoutUrl().toURI();
				if( casURI.equals(casLogoutURI) )
				{
					casLogoutURI = casURI.resolve(URI_LOGOUT);
				}
				casLoginUri = casURI.resolve(URI_LOGIN);
				casValidateUri = casURI.resolve(URI_VALIDATE);
				// FIXME dont do this
				BlindSSLSocketFactory.register();
			}
			catch( URISyntaxException e )
			{
				// can't happen
				assert false;
			}
			return true;
		}
		return false;
	}

	@Override
	public FilterResult filter(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		if( !CurrentUser.isGuest() || request.getParameter(PARAM_NOCAS) != null )
		{
			return null;
		}
		String ticket = request.getParameter(PARAM_TICKET);
		if( ticket == null )
		{
			QueryBuilder builder = urlService.getQueryBuilderForRequest(request);
			URI serviceUri = urlService.getUriForRequest(request, builder.toString());

			UriBuilder casUri = UriBuilder.create(casLoginUri);
			QueryBuilder casParams = QueryBuilder.create();
			if( casUri.getQuery() != null )
			{
				casParams.parse(casUri.getQuery());
			}
			casParams.addParam(PARAM_SERVICE, serviceUri.toString());
			casUri.setQuery(casParams);
			response.sendRedirect(casUri.toString());
			return FilterResult.FILTER_CONTINUE;
		}
		else
		{
			QueryBuilder builder = QueryBuilder.create();
			builder.parse(request.getQueryString());
			builder.removeParams(PARAM_TICKET);
			URI serviceUri = urlService.getUriForRequest(request, builder.toString());
			String username = validateTicket(ticket, serviceUri);
			userService.login(
				userService.authenticateAsUser(username, userService.getWebAuthenticationDetails(request)), false);
		}
		return null;
	}

	@Override
	public void addStateParameters(HttpServletRequest request, Map<String, String[]> params)
	{
		if( request.getParameter(PARAM_NOCAS) != null )
		{
			params.put(PARAM_NOCAS, new String[]{"true"});
		}
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
		loggedOutQB.addParam(PARAM_NOCAS, "true");
		UriBuilder builder = UriBuilder.create(loggedOutURI);
		builder.setQuery(loggedOutQB);
		return builder.build();
	}

	@Override
	public URI logoutRedirect(URI loggedOutURI)
	{
		UriBuilder casLogoutBuilder = UriBuilder.create(casLogoutURI);
		QueryBuilder logoutParams = QueryBuilder.create();
		if( casLogoutURI.getQuery() != null )
		{
			logoutParams.parse(casLogoutURI.getQuery());
		}
		Query logoutQuery = logoutParams.build();
		if( !logoutQuery.hasParam(PARAM_LOGOUT_URL) )
		{
			logoutParams.addParam(PARAM_LOGOUT_URL, loggedOutURI.toString());
			logoutQuery = logoutParams.build();
			casLogoutBuilder.setQuery(logoutQuery.toString());
		}
		return casLogoutBuilder.build();
	}

	private String validateTicket(String ticket, URI serviceUri)
	{
		QueryBuilder query = QueryBuilder.create();
		query.addParam(PARAM_TICKET, ticket);
		query.addParam(PARAM_SERVICE, serviceUri.toString());
		UriBuilder validateUri = UriBuilder.create(casValidateUri);
		validateUri.setQuery(query);

		try( BufferedReader reader = new BufferedReader(
			new InputStreamReader(new URL(validateUri.toString()).openStream())) )
		{
			String yesno = reader.readLine();
			if( yesno != null && yesno.equalsIgnoreCase(TICKET_VALIDATE_SUCCESS) )
			{
				return reader.readLine();
			}
			throw new AccessDeniedException("Failed to validate ticket with CAS server");
		}
		catch( IOException e )
		{
			LOGGER.error("Error validating CAS ticket", e);
			throw new RuntimeException(e);
		}
	}
}
