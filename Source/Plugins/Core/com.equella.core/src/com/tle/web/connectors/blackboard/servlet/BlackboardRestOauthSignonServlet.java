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

package com.tle.web.connectors.blackboard.servlet;

import com.dytech.devlib.Base64;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.common.PathUtils;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.blackboard.BlackboardRESTConnectorConstants;
import com.tle.core.connectors.blackboard.beans.Token;
import com.tle.core.connectors.blackboard.service.BlackboardRESTConnectorService;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.HttpService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Response;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.AuthenticationException;
import com.tle.web.oauth.response.ErrorResponse;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/** Served up at /blackboardrestauth */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
@Singleton
public class BlackboardRestOauthSignonServlet extends HttpServlet {
  private static final String STATE_CALLBACK_PARAMETER = "state";

  private static final Logger LOGGER = Logger.getLogger(BlackboardRestOauthSignonServlet.class);
  @Inject private HttpService httpService;
  @Inject private ConnectorService connectorService;
  @Inject private EncryptionService encryptionService;
  @Inject private ConfigurationService configService;
  @Inject private UserSessionService sessionService;
  @Inject private BlackboardRESTConnectorService blackboardRestConnectorService;
  @Inject private InstitutionService institutionService;

  private static final ObjectMapper jsonMapper = new ObjectMapper();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    LOGGER.trace("Requesting OAuth Sign-on");
    String postfixKey = "";
    String connectorUuid = "";
    String forwardUrl = null;
    String state = req.getParameter(STATE_CALLBACK_PARAMETER);

    if (state != null) {
      ObjectNode stateJson =
          (ObjectNode) new ObjectMapper().readTree(blackboardRestConnectorService.decrypt(state));
      JsonNode forwardUrlNode =
          stateJson.get(BlackboardRESTConnectorConstants.STATE_KEY_FORWARD_URL);
      if (forwardUrlNode != null) {
        forwardUrl = forwardUrlNode.asText();
      }

      JsonNode postfixKeyNode =
          stateJson.get(BlackboardRESTConnectorConstants.STATE_KEY_POSTFIX_KEY);
      if (postfixKeyNode != null) {
        postfixKey = postfixKeyNode.asText();
      }

      JsonNode connectorUuidNode = stateJson.get("connectorUuid");
      if (connectorUuidNode != null) {
        connectorUuid = connectorUuidNode.asText();
      }
    }
    String code = req.getParameter("code");
    sessionService.setAttribute(BlackboardRESTConnectorConstants.SESSION_CODE + postfixKey, code);

    // Ask for the token.
    final Connector connector = connectorService.getByUuid(connectorUuid);
    final String apiKey = connector.getAttribute(BlackboardRESTConnectorConstants.FIELD_API_KEY);
    final String apiSecret =
        encryptionService.decrypt(
            connector.getAttribute(BlackboardRESTConnectorConstants.FIELD_API_SECRET));
    final String b64 =
        new Base64()
            .encode((apiKey + ":" + apiSecret).getBytes())
            .replace("\n", "")
            .replace("\r", "");

    final Request oauthReq =
        new Request(
            PathUtils.urlPath(
                connector.getServerUrl(),
                "learn/api/public/v1/oauth2/token?code="
                    + code
                    + "&redirect_uri="
                    + institutionService.institutionalise(
                        BlackboardRESTConnectorConstants.AUTH_URL)));
    oauthReq.setMethod(Request.Method.POST);
    oauthReq.setMimeType("application/x-www-form-urlencoded");
    oauthReq.addHeader("Authorization", "Basic " + b64);
    oauthReq.setBody("grant_type=authorization_code");
    try (final Response resp2 =
        httpService.getWebContent(oauthReq, configService.getProxyDetails())) {
      if (resp2.isOk()) {
        LOGGER.trace("Blackboard response: " + resp2.getBody());
        final Token tokenJson = jsonMapper.readValue(resp2.getBody(), Token.class);
        LOGGER.warn("Gathered Blackboard access token for [" + connectorUuid + "]");
        blackboardRestConnectorService.setToken(connector, tokenJson.getAccessToken());
        blackboardRestConnectorService.setUserId(connector, tokenJson.getUserId());

      } else {
        final ErrorResponse bbErr = jsonMapper.readValue(resp2.getBody(), ErrorResponse.class);
        LOGGER.warn(
            "Unable to gather Blackboard access token for ["
                + connectorUuid
                + "] - Code ["
                + resp2.getCode()
                + "] - Msg ["
                + resp2.getMessage()
                + "] - Body ["
                + resp2.getBody()
                + "]");
        throw new AuthenticationException(
            "Unable to authenticate with Blackboard - " + bbErr.getErrorDescription());
      }
    } catch (Exception e) {
      LOGGER.warn(
          "Unable to gather Blackboard access token for ["
              + connectorUuid
              + "] - "
              + e.getMessage(),
          e);
      throw Throwables.propagate(e);
    }

    // close dialog OR redirect...
    if (forwardUrl != null) {
      resp.sendRedirect(forwardUrl);
    }
  }
}
