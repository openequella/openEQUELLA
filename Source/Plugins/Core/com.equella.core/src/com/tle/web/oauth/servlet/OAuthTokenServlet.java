/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import com.dytech.edge.exceptions.WebException;
import com.tle.common.Check;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.oauth.OAuthConstants;
import com.tle.web.oauth.OAuthException;
import com.tle.web.oauth.OAuthWebConstants;
import com.tle.web.oauth.response.ErrorResponse;
import com.tle.web.oauth.response.TokenResponse;
import com.tle.web.oauth.service.IOAuthClient;
import com.tle.web.oauth.service.IOAuthToken;
import com.tle.web.oauth.service.OAuthWebService;
import com.tle.web.oauth.service.OAuthWebService.AuthorisationDetails;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("nls")
@Bind
@Singleton
public class OAuthTokenServlet extends AbstractOAuthServlet {

  private static final long serialVersionUID = 1L;

  private static final String KEY_CLIENT_NOT_FOUND = "oauth.error.clientnotfound";
  private static final String REVOKE_ENDPOINT = "/oauth/revoke";
  private static final String TOKEN_PARAM = "token";

  @Inject private OAuthWebService oauthWebService;

  @Inject private EncryptionService encryptionService;

  @Override
  protected void doService(HttpServletRequest request, HttpServletResponse response)
      throws WebException {
    if (request.getServletPath().equals(REVOKE_ENDPOINT)) {
      revokeToken(request);
    } else {
      getOrCreateToken(request, response);
    }
  }

  @Override
  protected void respondWithError(
      HttpServletRequest request, HttpServletResponse response, int code, String error, Throwable t)
      throws IOException {
    final ErrorResponse r = new ErrorResponse();
    r.setError(error);
    r.setErrorDescription(t.getMessage());

    response.setStatus(code);
    response.setContentType("application/json");
    mapper.writeValue(response.getOutputStream(), r);
  }

  protected void respondWithToken(
      HttpServletResponse response, IOAuthToken token, String tokenType, String state)
      throws IOException {
    final TokenResponse r = new TokenResponse();
    r.setAccessToken(token.getToken());
    r.setTokenType(tokenType);
    r.setExpiresIn(token.getExpiry() == null ? Long.MAX_VALUE : calcExpiresIn(token.getExpiry()));
    r.setState(state);
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setContentType("application/json");
    mapper.writeValue(response.getOutputStream(), r);
  }

  private long calcExpiresIn(Instant expiry) {
    long now = Instant.now().toEpochMilli();
    long then = expiry.toEpochMilli();
    return then - now;
  }

  private void getOrCreateToken(HttpServletRequest request, HttpServletResponse response) {
    final String clientId = getParameter(request, OAuthWebConstants.PARAM_CLIENT_ID, true);

    // must be one of 'authorization_code' (4.1), 'client_credentials' (4.4)
    final String grantType = getParameter(request, OAuthWebConstants.PARAM_GRANT_TYPE, true);
    // 4.4
    final boolean isClientCredentials =
        (grantType != null && grantType.equals(OAuthWebConstants.GRANT_TYPE_CREDENTIALS));
    final boolean isAuthorizationCode =
        (grantType != null && grantType.equals(OAuthWebConstants.GRANT_TYPE_CODE));

    final String code = getParameter(request, OAuthWebConstants.PARAM_CODE, isAuthorizationCode);
    final String state = getParameter(request, OAuthWebConstants.PARAM_STATE, false);

    try {
      final AuthorisationDetails auth;
      final IOAuthClient client;
      // http://tools.ietf.org/html/draft-ietf-oauth-v2-23#section-4.1
      if (isAuthorizationCode) {
        final String redirectUrl =
            getParameter(request, OAuthWebConstants.PARAM_REDIRECT_URI, true);
        client = oauthWebService.getByClientIdAndRedirectUrl(clientId, redirectUrl);
        if (client == null) {
          throw new OAuthException(
              400,
              OAuthConstants.ERROR_INVALID_CLIENT,
              text(KEY_CLIENT_NOT_FOUND, clientId, redirectUrl),
              true);
        }
        auth = oauthWebService.getAuthorisationDetailsByCode(client, code);
      }
      // http://tools.ietf.org/html/draft-ietf-oauth-v2-23#section-4.4
      else if (isClientCredentials) {
        client = oauthWebService.getByClientIdOnly(clientId);
        if (client == null) {
          throw new OAuthException(
              400,
              OAuthConstants.ERROR_INVALID_CLIENT,
              text("oauth.error.clientnotfoundnoredir", clientId),
              true);
        }
        final String clientSecret =
            getParameter(request, OAuthWebConstants.PARAM_CLIENT_SECRET, true);
        auth = oauthWebService.getAuthorisationDetailsBySecret(client, clientSecret);

        if (Check.isEmpty(client.getUserId())) {
          throw new OAuthException(
              400, OAuthConstants.ERROR_INVALID_CLIENT, text("oauth.error.musthavefixeduser"));
        }
      } else {
        // Invalid grant type
        throw new OAuthException(
            400,
            OAuthConstants.ERROR_UNSUPPORTED_GRANT_TYPE,
            text("oauth.error.invalidgranttype", grantType));
      }

      try {
        final IOAuthToken token = oauthWebService.getOrCreateToken(auth, client, code);

        respondWithToken(response, token, OAuthWebConstants.TOKEN_TYPE_EQUELLA_API, state);
      } catch (Exception e) // NOSONAR
      {
        String msg = e.getLocalizedMessage();
        throw new OAuthException(500, OAuthConstants.ERROR_SERVER_ERROR, msg);
      }
    } finally {
      if (code != null) {
        oauthWebService.invalidateCode(code);
      }
    }
  }

  private boolean validateCredentials(String auth) {
    if (auth.toLowerCase().startsWith(OAuthWebConstants.BASIC_AUTHORIZATION_PREFIX)) {
      // Remove the prefix `Basic `.
      byte[] decoded = Base64.getDecoder().decode(auth.substring(6));
      String[] credentials = new String(decoded).split(":");
      if (credentials.length >= 2) {
        String clientId = credentials[0];
        String clientSecret = credentials[1];

        IOAuthClient client = oauthWebService.getByClientIdOnly(clientId);

        return Optional.ofNullable(client)
            .map(IOAuthClient::getClientSecret)
            .map(secret -> encryptionService.decrypt(secret))
            .map(secret -> secret.equals(clientSecret))
            .orElse(false);
      }
    }

    return false;
  }

  /**
   * Revoke an Oauth token. Client credentials must be validated first. If the validation fails,
   * return a 401 error.
   *
   * <p>According to <a href="https://tools.ietf.org/html/rfc7009#section-2.2">the spec for OAuth
   * 2.0 Token Revocation </a>>, the response code should be 200 regardless whether the token is
   * valid or not. Hence, token validation is not needed. However, if a token is not present in the
   * request payload, return a 400 error.
   */
  private void revokeToken(HttpServletRequest request) {
    Optional.ofNullable(request.getHeader(OAuthWebConstants.HEADER_AUTHORIZATION))
        .filter(this::validateCredentials)
        .ifPresentOrElse(
            (auth) -> {
              String token = getParameter(request, TOKEN_PARAM, true);
              oauthWebService.revokeToken(token);
            },
            () -> {
              throw new OAuthException(
                  HttpServletResponse.SC_UNAUTHORIZED,
                  OAuthConstants.ERROR_INVALID_REQUEST,
                  text("oauth.error.validationfailed"));
            });
  }
}
