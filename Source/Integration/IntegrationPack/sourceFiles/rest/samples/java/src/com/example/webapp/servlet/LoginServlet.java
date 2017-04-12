/*
 * Copyright (c) 2011, EQUELLA All rights reserved. Redistribution and use in
 * source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met: Redistributions of source code must
 * retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. Neither
 * the name of EQUELLA nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.example.webapp.servlet;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.node.ObjectNode;

import com.example.service.Config;
import com.example.service.JsonMapper;
import com.example.service.WebClient;
import com.example.util.UrlUtils;

/**
 * /login Obtains an OAuth token from the EQUELLA server. Never shows a 'native'
 * login page, it redirects to the EQUELLA login page so that the user is forced
 * to login to EQUELLA The client_id and home (redirect_uri) as found on
 * config.properties, must be registered in EQUELLA first.
 */
public class LoginServlet extends AbstractServlet
{
	private static final long serialVersionUID = 1L;

	private static final String KEY_STATE = "$state";

	private static final String PARAM_STATE = "state";
	private static final String PARAM_CODE = "code";
	private static final String PARAM_ERROR = "error";
	private static final String PARAM_ERROR_DESCRIPTION = "error_description";

	@Override
	protected String getTemplateFilename()
	{
		// Login page has no renderable content, user is redirected to the
		// EQUELLA login page
		return null;
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		// Was an error passed back from EQUELLA during the OAuth authorise
		// request?

		final String error = request.getParameter(PARAM_ERROR);
		if( error != null )
		{
			// Note: errors involving invalid client IDs and redirect URIs will
			// not be redirected back
			// to this client (as per the OAuth 2.0 specification) and EQUELLA
			// will display the error
			// directly to the user

			final String errorDescription = request.getParameter(PARAM_ERROR_DESCRIPTION);
			throw new ServletException("An error was received from EQUELLA -- " + error + ": " + errorDescription);
		}

		// OAuth redirection

		final String code = request.getParameter(PARAM_CODE);
		final HttpSession session = request.getSession();

		if( code == null )
		{
			// There is no 'code' or 'error' parameter in the request, therefore
			// this request did not come from EQUELLA,
			// so we need to establish a login

			// The state parameter uniquely identifies this conversation with
			// EQUELLA. EQUELLA will pass it back in its response.
			final String state = UUID.randomUUID().toString();
			session.setAttribute(KEY_STATE, state);

			// Request a code which we can use to request a token later
			final String authUrl = Config.getEquellaUrl() + "oauth/authorise?response_type=code&client_id="
				+ Config.getClientId() + "&redirect_uri=" + UrlUtils.urlParamEncode(Config.getHome() + "login")
				+ "&state=" + UrlUtils.urlParamEncode(state);
			response.sendRedirect(authUrl);
			return;
		}

		// Check the 'state' parameter and make sure it's the same state we
		// setup earlier

		final String state = (String) session.getAttribute(KEY_STATE);
		if( state != null && state.equals(request.getParameter(PARAM_STATE)) )
		{
			// ask for a token from EQUELLA using the code that was returned
			// from EQUELLA

			final String tokenUrl = Config.getEquellaUrl()
				+ "oauth/access_token?grant_type=authorization_code&client_id=" + Config.getClientId()
				+ "&redirect_uri=" + UrlUtils.urlParamEncode(Config.getHome() + "login") + "&code="
				+ UrlUtils.urlParamEncode(code);

			final HttpResponse r = WebClient.execute(new HttpGet(tokenUrl), false, null);
			final ObjectNode tokenNode = JsonMapper.readJson(r);
			final String token = tokenNode.get("access_token").getValueAsText();

			// add token to our user's session
			session.setAttribute(KEY_TOKEN, token);

			// remove the state information, we're done with it
			session.removeAttribute(KEY_STATE);

			// forward to our main page
			response.sendRedirect(Config.getHome() + "search");
			return;
		}

		// 'state' parameter was unknown, possibly a hijack attempt

		throw new RuntimeException(
			"The state parameter does not match the state associated with this session.  This could indicate an XSRF attempt.");
	}
}
