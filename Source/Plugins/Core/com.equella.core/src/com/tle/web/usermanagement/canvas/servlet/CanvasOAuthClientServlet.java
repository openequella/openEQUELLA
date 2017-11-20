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

package com.tle.web.usermanagement.canvas.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dytech.edge.web.WebConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import com.tle.beans.usermanagement.canvas.CanvasWrapperSettings;
import com.tle.common.PathUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.connectors.canvas.service.CanvasConnectorService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.HttpService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Request.FormParameters;
import com.tle.core.services.http.Request.Method;
import com.tle.core.services.http.Response;
import com.tle.core.services.user.UserService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.UsernameNotFoundException;
import com.tle.web.login.LogonSection;
import com.tle.web.sections.SectionsController;

/**
 * <p>
 * Found at http://inst/canvassso
 * </p>
 * <p>
 * When Equella is acting as an OAuth client, this is the redirect URL you
 * should specify. Basically, it's a copy of the sample REST client LoginServlet
 * </p>
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class CanvasOAuthClientServlet extends HttpServlet
{
	private static final String PARAM_CODE = "code";
	private static final String PARAM_ERROR = "error";

	private final ObjectMapper mapper = new ObjectMapper();

	@Inject
	private ConfigurationService configService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private HttpService httpService;
	@Inject
	private UserService userService;
	@Inject
	private CanvasConnectorService connectorService;
	@Inject
	private SectionsController sectionsController;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		// Was an error passed back from EQUELLA during the OAuth authorise
		// request?

		final String error = request.getParameter(PARAM_ERROR);
		if( error != null )
		{
			// return to login screen with error message present
			request.setAttribute(WebConstants.KEY_LOGIN_EXCEPTION,
				new Exception(CurrentLocale.get("com.tle.web.usermanagement.canvas.error.canvaserror", error)));
			LogonSection.forwardToLogon(sectionsController, request, response, null, LogonSection.STANDARD_LOGON_PATH);
			return;
		}

		// OAuth redirection

		final String code = request.getParameter(PARAM_CODE);

		final CanvasWrapperSettings settings = configService.getProperties(new CanvasWrapperSettings());

		if( code == null )
		{
			// There is no 'code' or 'error' parameter in the request, therefore
			// this request did not come from Canvas,
			// so we need to establish a login

			// Request a code which we can use to request a token later
			final Request codeRequest = new Request(PathUtils.urlPath(settings.getCanvasUrl(), "login/oauth2/auth"));
			// codeRequest.addParameter("scopes", "/auth/userinfo");
			codeRequest.addParameter("response_type", "code");
			codeRequest.addParameter("client_id", settings.getClientId());
			codeRequest.addParameter("redirect_uri", getRedirectUri(request));

			response.sendRedirect(httpService.toUrl(codeRequest));
			return;
		}

		// ask for a token (actually a User Auth) from Canvas using the code
		// that was returned
		// from Canvas

		final Request tokenRequest = new Request(PathUtils.urlPath(settings.getCanvasUrl(), "login/oauth2/token"));
		tokenRequest.setMethod(Method.POST);
		final FormParameters formParams = new FormParameters();
		formParams.addParameter("client_id", settings.getClientId());
		formParams.addParameter("client_secret", settings.getClientSecret());
		formParams.addParameter("redirect_uri", institutionService.institutionalise("canvassso"));
		formParams.addParameter("code", code);
		tokenRequest.setHtmlForm(formParams);

		final String token;
		try( final Response tokenResponse = httpService.getWebContent(tokenRequest, configService.getProxyDetails()) )
		{
			if( !tokenResponse.isOk() )
			{
				throw new RuntimeException("Problem retrieving token from Canvas (" + tokenResponse.getCode() + "): "
					+ tokenResponse.getBody());
			}
			final ObjectNode tokenNode = (ObjectNode) mapper.readTree(tokenResponse.getInputStream());
			token = tokenNode.get("access_token").asText();
		}

		// Call canvas API to user details?
		final Request apiRequest = new Request(PathUtils.urlPath(settings.getCanvasUrl(), "api/v1/users/self/profile"));
		try( Response userResponse = connectorService.getCanvasResponse(apiRequest, token, null) )
		{
			final ObjectNode userNode = (ObjectNode) mapper.readTree(userResponse.getInputStream());

			final String fullName = userNode.get("name").asText();
			final String username = userNode.get("login_id").asText();
			final String email = userNode.get("primary_email").asText();
			userService.login(
				userService.authenticateAsUser(username, userService.getWebAuthenticationDetails(request)), true);
		}
		catch( UsernameNotFoundException unnfe )
		{
			// kick to login screen with error
			request.setAttribute(WebConstants.KEY_LOGIN_EXCEPTION, unnfe);
			LogonSection.forwardToLogon(sectionsController, request, response, null, LogonSection.STANDARD_LOGON_PATH);
		}

		// forward to requested page
		String page = request.getParameter("page");
		if( page == null )
		{
			page = "home.do";
		}
		response.sendRedirect(institutionService.institutionalise(page));
	}

	private String getRedirectUri(HttpServletRequest request)
	{
		final String baseUrl = institutionService.institutionalise("canvassso");
		final String page = request.getParameter("page");
		if( page != null )
		{
			try
			{
				return baseUrl + "?page=" + URLEncoder.encode(page, "UTF-8");
			}
			catch( UnsupportedEncodingException e )
			{
				throw Throwables.propagate(e);
			}
		}
		return baseUrl;
	}
}
