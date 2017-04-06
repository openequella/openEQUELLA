package com.tle.web.lti.service;

import javax.servlet.http.HttpServletResponse;

import com.tle.web.lti.LtiData;
import com.tle.web.lti.imsx.ImsxPOXEnvelopeType;

/**
 * @author Aaron
 */
public interface LtiService
{
	void sendGrade(LtiData ltiData, String grade);

	void sendGrade(ImsxPOXEnvelopeType envelope, String consumerKey, String secret, String url,
		HttpServletResponse servletResponse);
}
