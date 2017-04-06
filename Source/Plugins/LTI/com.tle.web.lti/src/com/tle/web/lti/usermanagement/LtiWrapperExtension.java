package com.tle.web.lti.usermanagement;

import javax.servlet.http.HttpServletRequest;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface LtiWrapperExtension
{
	@Nullable
	String getUserId(HttpServletRequest request);

	@Nullable
	String getUsername(HttpServletRequest request);

	@Nullable
	String getFirstName(HttpServletRequest request);

	@Nullable
	String getLastName(HttpServletRequest request);

	@Nullable
	String getEmail(HttpServletRequest request);
}
