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

package com.tle.web.oauth.service;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;

import com.dytech.edge.exceptions.WebException;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.core.oauth.OAuthUserState;
import com.tle.common.usermanagement.user.UserState;

/**
 * @author Aaron
 */
public interface OAuthWebService
{
	String createCode(OAuthClient client, AuthorisationDetails details) throws WebException;

	/**
	 * User details are extracted from the code
	 * 
	 * @param client
	 * @param code
	 * @return
	 * @throws WebException
	 */
	AuthorisationDetails getAuthorisationDetailsByCode(OAuthClient client, String code) throws WebException;

	/**
	 * Uses the user details of the pre-configured user
	 * 
	 * @param clientId
	 * @param clientSecret
	 * @return
	 * @throws WebException
	 */
	AuthorisationDetails getAuthorisationDetailsBySecret(OAuthClient client, String clientSecret) throws WebException;

	/**
	 * Requires that the user login to EQUELLA if the client is not
	 * pre-configured for a user
	 * 
	 * @param client
	 * @return
	 * @throws WebException
	 */
	AuthorisationDetails getAuthorisationDetailsByUserState(OAuthClient client, UserState userState)
		throws WebException;

	/**
	 * @param tokenData
	 * @param request
	 * @return Never returns null.
	 * @throws WebException thrown if token not found
	 */
	OAuthUserState getUserState(String tokenData, HttpServletRequest request) throws WebException;

	/**
	 * Ensures this code cannot be used again
	 * 
	 * @param code .
	 */
	boolean invalidateCode(String code);

	void validateMessage(OAuthMessage message, OAuthAccessor accessor) throws OAuthException, IOException,
		URISyntaxException;

	public static class AuthorisationDetails
	{
		private String userId;
		private String username;
		private boolean requiresLogin;

		public String getUserId()
		{
			return userId;
		}

		public void setUserId(String userId)
		{
			this.userId = userId;
		}

		/**
		 * Not guaranteed to have any value!
		 * 
		 * @return
		 */
		public String getUsername()
		{
			return username;
		}

		public void setUsername(String username)
		{
			this.username = username;
		}

		public boolean isRequiresLogin()
		{
			return requiresLogin;
		}

		public void setRequiresLogin(boolean requiresLogin)
		{
			this.requiresLogin = requiresLogin;
		}
	}
}