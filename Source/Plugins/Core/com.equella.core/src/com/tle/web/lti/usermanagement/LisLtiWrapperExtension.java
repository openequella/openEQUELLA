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

package com.tle.web.lti.usermanagement;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.core.guice.Bind;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class LisLtiWrapperExtension implements LtiWrapperExtension
{
	@Override
	public String getUserId(HttpServletRequest request)
	{
		return null;
	}

	@Override
	public String getUsername(HttpServletRequest request)
	{
		return request.getParameter(ExternalToolConstants.LIS_PERSON_SOURCEDID);
	}

	@Override
	public String getFirstName(HttpServletRequest request)
	{
		return request.getParameter(ExternalToolConstants.LIS_PERSON_NAME_GIVEN);
	}

	@Override
	public String getLastName(HttpServletRequest request)
	{
		return request.getParameter(ExternalToolConstants.LIS_PERSON_NAME_FAMILY);
	}

	@Override
	public String getEmail(HttpServletRequest request)
	{
		return request.getParameter(ExternalToolConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY);
	}

	@Override
	public boolean isPrefixUserId()
	{
		return true;
	}
}
