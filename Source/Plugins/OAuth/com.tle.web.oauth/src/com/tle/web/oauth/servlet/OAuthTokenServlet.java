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

package com.tle.web.oauth.servlet;

import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dytech.edge.exceptions.WebException;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.core.guice.Bind;
import com.tle.core.oauth.OAuthConstants;
import com.tle.core.oauth.service.OAuthService;
import com.tle.core.services.user.UserService;
import com.tle.web.oauth.OAuthException;
import com.tle.web.oauth.OAuthWebConstants;
import com.tle.web.oauth.response.ErrorResponse;
import com.tle.web.oauth.response.TokenResponse;
import com.tle.web.oauth.service.OAuthWebService;
import com.tle.web.oauth.service.OAuthWebService.AuthorisationDetails;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class OAuthTokenServlet extends AbstractOAuthServlet
{
	private static final long serialVersionUID = 1L;

	private static final String KEY_CLIENT_NOT_FOUND = PREFIX + "oauth.error.clientnotfound";

	@Inject
	private UserService userService;
	@Inject
	private OAuthWebService oauthWebService;
	@Inject
	private OAuthService oauthService;


	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException, WebException
	{
		final String clientId = getParameter(request, OAuthWebConstants.PARAM_CLIENT_ID, true);
		final String redirectUrl = getParameter(request, OAuthWebConstants.PARAM_REDIRECT_URI, true);

		final OAuthClient client = oauthService.getByClientIdAndRedirectUrl(clientId, redirectUrl);
		if( client == null )
		{
			throw new OAuthException(400, OAuthConstants.ERROR_INVALID_CLIENT, CurrentLocale.get(KEY_CLIENT_NOT_FOUND,
				clientId, redirectUrl), true);
		}

		// must be one of 'authorization_code' (4.1), 'client_credentials' (4.4)
		final String grantType = getParameter(request, OAuthWebConstants.PARAM_GRANT_TYPE, true);
		// 4.4
		final boolean isClientCredentials = (grantType != null && grantType
			.equals(OAuthWebConstants.GRANT_TYPE_CREDENTIALS));
		final boolean isAuthorizationCode = (grantType != null && grantType.equals(OAuthWebConstants.GRANT_TYPE_CODE));

		final String code = getParameter(request, OAuthWebConstants.PARAM_CODE, isAuthorizationCode);
		final String state = getParameter(request, OAuthWebConstants.PARAM_STATE, false);

		try
		{
			final AuthorisationDetails auth;
			// http://tools.ietf.org/html/draft-ietf-oauth-v2-23#section-4.1
			if( isAuthorizationCode )
			{
				auth = oauthWebService.getAuthorisationDetailsByCode(client, code);
			}
			// http://tools.ietf.org/html/draft-ietf-oauth-v2-23#section-4.4
			else if( isClientCredentials )
			{
				final String clientSecret = getParameter(request, OAuthWebConstants.PARAM_CLIENT_SECRET, true);
				auth = oauthWebService.getAuthorisationDetailsBySecret(client, clientSecret);

				if( Check.isEmpty(client.getUserId()) )
				{
					throw new OAuthException(400, OAuthConstants.ERROR_INVALID_CLIENT, CurrentLocale.get(PREFIX
						+ "oauth.error.musthavefixeduser"));
				}
			}
			else
			{
				// Invalid grant type
				throw new OAuthException(400, OAuthConstants.ERROR_UNSUPPORTED_GRANT_TYPE, CurrentLocale.get(PREFIX
					+ "oauth.error.invalidgranttype", grantType));
			}

			String username = auth.getUsername();
			if( username == null )
			{
				username = userService.getInformationForUser(auth.getUserId()).getUsername();
			}
			try
			{
				final OAuthToken token = oauthService.getOrCreateToken(auth.getUserId(), username, client, code);

				respondWithToken(response, token, OAuthWebConstants.TOKEN_TYPE_EQUELLA_API, state);
			}
			catch( Exception e ) // NOSONAR
			{
				String msg = e.getLocalizedMessage();
				throw new OAuthException(500, OAuthConstants.ERROR_SERVER_ERROR, msg);
			}
		}
		finally
		{
			if( code != null )
			{
				oauthWebService.invalidateCode(code);
			}
		}
	}

	@Override
	protected void respondWithError(HttpServletRequest request, HttpServletResponse response, int code, String error,
		Throwable t) throws IOException, ServletException
	{
		final ErrorResponse r = new ErrorResponse();
		r.setError(error);
		r.setErrorDescription(t.getMessage());

		response.setStatus(code);
		response.setContentType("application/json");
		mapper.writeValue(response.getOutputStream(), r);
	}

	protected void respondWithToken(HttpServletResponse response, OAuthToken token, String tokenType, String state)
		throws IOException
	{
		final TokenResponse r = new TokenResponse();
		r.setAccessToken(token.getToken());
		r.setTokenType(tokenType);
		r.setExpiresIn(token.getExpiry() == null ? Long.MAX_VALUE : calcExpiresIn(token.getExpiry()));
		r.setState(state);
		response.setContentType("application/json");
		mapper.writeValue(response.getOutputStream(), r);
	}

	private long calcExpiresIn(Date expiry)
	{
		long now = new Date().getTime();
		long then = expiry.getTime();
		return then - now;
	}
}
