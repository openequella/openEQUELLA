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

package com.tle.integration.lti.brightspace;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.dytech.devlib.Md5;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.tle.common.Utils;
import com.tle.core.guice.Bind;
import com.tle.web.lti.usermanagement.LtiWrapperExtension;

/**
 * Checks for Brightspace ext_d2l_username param to match an existing
 * user.
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class BrightspaceLtiWrapperExtension implements LtiWrapperExtension
{
	@Named("brightspace.parameter.username")
	@Inject(optional = true)
	private String usernameParameter;
	@Named("brightspace.parameter.userid")
	@Inject(optional = true)
	private String userIdParameter;

	@Override
	public String getUserId(HttpServletRequest request)
	{
		final String rawId = request.getParameter(userIdParameter);
		if( !Strings.isNullOrEmpty(rawId) )
		{
			return truncatedUniqueName(rawId);
		}
		// Fall back to username param (which may mean username and user ID are the same, which is fine)
		final String rawUsername = getUsername(request);
		if( !Strings.isNullOrEmpty(rawUsername) )
		{
			return truncatedUniqueName(rawUsername);
		}
		return null;
	}

	/**
	 * EQUELLA only allows 40 chars for user IDs.
	 * We hope that this is vaguely unique.
	 * 
	 * @param rawName
	 * @return
	 */
	private String truncatedUniqueName(String rawName)
	{
		int rawIdLength = rawName.length();
		if( rawIdLength >= 40 )
		{
			//14 chars from front, (rest of chars hashed = 11 chars), 14 chars from end 
			final String head = Utils.safeSubstring(rawName, 0, 14);
			final String tail = Utils.safeSubstring(rawName, -14);
			final String hashedMiddle = Utils
				.safeSubstring(new Md5(Utils.safeSubstring(rawName, 14, -14)).getStringDigest(), 0, 11);

			final StringBuilder result = new StringBuilder(head);
			result.append(hashedMiddle);
			result.append(tail);
			return result.toString();

		}
		return rawName;
	}

	@Override
	public String getUsername(HttpServletRequest request)
	{
		return request.getParameter(usernameParameter);
	}

	/**
	 * Fallback to standard LIS params
	 */
	@Override
	public String getFirstName(HttpServletRequest request)
	{
		return null;
	}

	/**
	 * Fallback to standard LIS params
	 */
	@Override
	public String getLastName(HttpServletRequest request)
	{
		return null;
	}

	/**
	 * Fallback to standard LIS params
	 */
	@Override
	public String getEmail(HttpServletRequest request)
	{
		return null;
	}

	@Override
	public boolean isPrefixUserId()
	{
		return false;
	}
}
