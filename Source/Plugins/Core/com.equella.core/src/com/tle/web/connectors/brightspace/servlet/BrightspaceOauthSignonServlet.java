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

package com.tle.web.connectors.brightspace.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.annotation.NonNullByDefault;
import com.tle.core.connectors.brightspace.BrightspaceConnectorConstants;
import com.tle.core.connectors.brightspace.service.BrightspaceConnectorService;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserSessionService;

/**
 * Served up at /brightspaceauth
 * 
 * @author Aaron
 *
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
@Singleton
public class BrightspaceOauthSignonServlet extends HttpServlet
{
	private static final String USER_ID_CALLBACK_PARAMETER = "x_a";
	private static final String USER_KEY_CALLBACK_PARAMETER = "x_b";
	private static final String STATE_CALLBACK_PARAMETER = "x_state";

	@Inject
	private UserSessionService sessionService;
	@Inject
	private BrightspaceConnectorService brightspaceConnectorService;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String postfixKey = "";
		String forwardUrl = null;

		String state = req.getParameter(STATE_CALLBACK_PARAMETER);
		if( state != null )
		{
			ObjectNode stateJson = (ObjectNode) new ObjectMapper().readTree(brightspaceConnectorService.decrypt(state));
			JsonNode forwardUrlNode = stateJson.get(BrightspaceConnectorConstants.STATE_KEY_FORWARD_URL);
			if( forwardUrlNode != null )
			{
				forwardUrl = forwardUrlNode.asText();
			}

			JsonNode postfixKeyNode = stateJson.get(BrightspaceConnectorConstants.STATE_KEY_POSTFIX_KEY);
			if( postfixKeyNode != null )
			{
				postfixKey = postfixKeyNode.asText();
			}
		}

		sessionService.setAttribute(BrightspaceConnectorConstants.SESSION_KEY_USER_ID + postfixKey,
			req.getParameter(USER_ID_CALLBACK_PARAMETER));
		sessionService.setAttribute(BrightspaceConnectorConstants.SESSION_KEY_USER_KEY + postfixKey,
			req.getParameter(USER_KEY_CALLBACK_PARAMETER));

		//close dialog OR redirect...
		if( forwardUrl != null )
		{
			resp.sendRedirect(forwardUrl);
		}
	}
}
