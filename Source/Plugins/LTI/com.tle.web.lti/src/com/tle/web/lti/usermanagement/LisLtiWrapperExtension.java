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
