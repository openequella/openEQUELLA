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

package com.tle.web.connectors.brightspace.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.annotation.NonNullByDefault;
import com.tle.common.util.UriUtils;
import com.tle.core.connectors.brightspace.BrightspaceConnectorConstants;
import com.tle.core.connectors.brightspace.service.BrightspaceConnectorService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.user.UserSessionService;
import java.io.IOException;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Served up at /brightspaceauth */
@NonNullByDefault
@Bind
@Singleton
public class BrightspaceOauthSignonServlet extends HttpServlet {
  private static final Logger LOGGER = LoggerFactory.getLogger(BrightspaceOauthSignonServlet.class);

  private static final String USER_ID_CALLBACK_PARAMETER = "x_a";
  private static final String USER_KEY_CALLBACK_PARAMETER = "x_b";
  private static final String STATE_CALLBACK_PARAMETER = "x_state";

  private final UserSessionService sessionService;
  private final BrightspaceConnectorService brightspaceConnectorService;
  private final InstitutionService institutionService;

  @Inject
  public BrightspaceOauthSignonServlet(
      UserSessionService sessionService,
      BrightspaceConnectorService brightspaceConnectorService,
      InstitutionService institutionService) {
    this.sessionService = sessionService;
    this.brightspaceConnectorService = brightspaceConnectorService;
    this.institutionService = institutionService;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String postfixKey = "";
    String forwardUrl = null;

    String state = req.getParameter(STATE_CALLBACK_PARAMETER);
    if (state != null) {
      ObjectNode stateJson =
          (ObjectNode) new ObjectMapper().readTree(brightspaceConnectorService.decrypt(state));
      JsonNode forwardUrlNode = stateJson.get(BrightspaceConnectorConstants.STATE_KEY_FORWARD_URL);
      if (forwardUrlNode != null) {
        forwardUrl = forwardUrlNode.asText();
      }

      JsonNode postfixKeyNode = stateJson.get(BrightspaceConnectorConstants.STATE_KEY_POSTFIX_KEY);
      if (postfixKeyNode != null) {
        postfixKey = postfixKeyNode.asText();
      }
    }

    sessionService.setAttribute(
        BrightspaceConnectorConstants.SESSION_KEY_USER_ID + postfixKey,
        req.getParameter(USER_ID_CALLBACK_PARAMETER));
    sessionService.setAttribute(
        BrightspaceConnectorConstants.SESSION_KEY_USER_KEY + postfixKey,
        req.getParameter(USER_KEY_CALLBACK_PARAMETER));

    // close dialog OR redirect...
    if (forwardUrl == null) {
      return;
    }

    switch (resolveForwardUrl(forwardUrl)) {
      case ForwardUrlResolution.Redirect(URI target) -> resp.sendRedirect(target.toString());
      case ForwardUrlResolution.Rejected(String reason) -> {
        LOGGER.warn("Rejected {} forward URL: {}", reason, forwardUrl);
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid forward URL");
      }
    }
  }

  /**
   * The outcome of validating a decrypted forward URL: either a target that is safe to redirect to,
   * or a rejection carrying the reason it was refused (used for server-side logging only).
   */
  private sealed interface ForwardUrlResolution {
    record Redirect(URI target) implements ForwardUrlResolution {}

    record Rejected(String reason) implements ForwardUrlResolution {}
  }

  /**
   * Parses the raw forward URL string and resolves it against the institution, returning either a
   * safe {@link ForwardUrlResolution.Redirect} target or a {@link ForwardUrlResolution.Rejected}
   * describing why it was refused.
   */
  private ForwardUrlResolution resolveForwardUrl(String forwardUrl) {
    final URI parsed;
    try {
      parsed = URI.create(forwardUrl);
    } catch (IllegalArgumentException e) {
      return new ForwardUrlResolution.Rejected("malformed");
    }

    return resolveParsedForwardUrl(parsed);
  }

  /**
   * Resolves an already-parsed forward URI against the institution base URL. Returns a {@link
   * ForwardUrlResolution.Redirect} only if it stays within the institution's own
   * scheme/host/port/path; otherwise a {@link ForwardUrlResolution.Rejected} marked "unsafe". This
   * covers absolute URLs to other hosts, protocol-relative URLs (//evil.example), scheme
   * downgrades, non-http(s) schemes (javascript:, data:, etc.) and paths that use ".." to escape
   * the institution. Safety (including path normalization) is decided by {@link
   * UriUtils#isSafeRedirectUri}.
   */
  private ForwardUrlResolution resolveParsedForwardUrl(URI forwardUri) {
    final URI base = institutionService.getInstitutionUri();
    final URI target = base.resolve(forwardUri);

    return UriUtils.isSafeRedirectUri(base, target)
        ? new ForwardUrlResolution.Redirect(target)
        : new ForwardUrlResolution.Rejected("unsafe");
  }
}
