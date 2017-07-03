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

package com.tle.web.externaltools.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.dytech.edge.exceptions.WebException;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.tle.common.Check;
import com.tle.common.externaltools.SourcedIdPackage;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.lti.consumers.service.LtiConsumerService;
import com.tle.core.oauth.OAuthConstants;
import com.tle.web.lti.imsx.ImsxPOXEnvelopeType;
import com.tle.web.lti.service.LtiService;

//@formatter:off

/**
 * The presumed schema for request data:
 *
 *<imsx_POXEnvelopeRequest>
 *	<imsx_POXHeader>
 *		<imsx_POXRequestHeaderInfo>
 *			<imsx_version>V1.0</imsx_version>
 *			<imsx_messageIdentifier>---some UUID ---</imsx_messageIdentifier>
 *		</imsx_POXRequestHeaderInfo>
 *	</imsx_POXHeader>
 *	<imsx_POXBody>
 *		<replaceResultRequest>
 *			<resultRecord>
 *				<sourcedGUID>
 *					<sourcedId>--- the arbitrary string data as sent to Icodeon launch---</sourcedId>
 *				</sourcedGUID>
 *				<result>
 *					<resultScore>
 *						<language>en</language>
 *						<textString>--the numeric score 0.0 -- 1.0---</textString>
 *					</resultScore>
 *				</result>
 *			</resultRecord>
 *		</replaceResultRequest>
 *	</imsx_POXBody>
 *</imsx_POXEnvelopeRequest>
 *
 * The first version of this class submitted for testing is built on the premise
 * that EQUELLA needs to work around Icodeon's limitations in that when Icodeon
 * firstly receives the LTI launch request, including the all-important
 * lis_result_sourced parameter:<br>
 * - Icodeon can't deal with a JSON format string in the parameter values.<br>
 * - (supposedly fixed in Icodeon build #3149, but unverified due to SQL error)<br>
 * - Icodeon can't deal with newlines in the parameter values.<br>
 * - Icodeon silently imposes a 512 character limit by truncating anything longer,<br>
 *     thus rendering useless the parameter value for lis_result_sourcedid.<br>
 * The workaround solution is to strip any newlines in parameter values.<br>
 * - Base64 encoding no longer engaged - assuming that Icodeon has fixed its JSON indigestion.
 * The truncation over 512 characters remains a hazard. This code is submitted on the hypothesis
 * that neither Moodle nor any other LMS will put together a lis_result_sourcedid value that
 * (when extended by EQUELLA) won't push the 512 limit.
 *@author larry
 *
 */
//@formatter:on

@Bind
@Singleton
@SuppressWarnings("nls")
public class ResultOutcomeServlet extends HttpServlet
{
	private static final long serialVersionUID = 545872120021L;
	private static final Logger LOGGER = Logger.getLogger(ResultOutcomeServlet.class);

	@Inject
	private LtiService ltiService;
	@Inject
	private LtiConsumerService ltiConsumerService;
	@Inject
	private EncryptionService encryptionService;

	/**
	 * The gradebook update from Icodeon will be in an XML stream
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws WebException
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		LOGGER.debug("Received request to process grades...");
		ImsxPOXEnvelopeType env = null;
		try
		{
			final JAXBContext jc = JAXBContext.newInstance("com.tle.web.lti.imsx",
				ResultOutcomeServlet.class.getClassLoader());
			final Unmarshaller unmarshaller = jc.createUnmarshaller();
			Object unmarshalled = unmarshaller.unmarshal(req.getReader());
			if( !(unmarshalled instanceof JAXBElement) )
			{
				throw new ServletException("Unexpected object in unmarshalled request data: "
					+ (unmarshalled == null ? "(null)" : unmarshalled.getClass().getSimpleName()));
			}
			JAXBElement<?> ummagumma = (JAXBElement<?>) unmarshalled;
			if( !ummagumma.getDeclaredType().equals(ImsxPOXEnvelopeType.class) )
			{
				throw new ServletException(
					"Unexpected object in unmarshalled request data: " + unmarshalled.getClass().getSimpleName());
			}
			env = ((JAXBElement<ImsxPOXEnvelopeType>) ummagumma).getValue();
		}
		catch( JAXBException jbex )
		{
			throw new ServletException("Unexpectedly failure to read envelope in request", jbex);
		}
		String sourcedId = env.getImsxPOXBody().getReplaceResultRequest().getResultRecord().getSourcedGUID()
			.getSourcedId();

		if( Check.isEmpty(sourcedId) )
		{
			throw new ServletException("Unexpectedly empty raw sourcedId value in request");
		}

		String lmsoauthkey = null;
		String clientSecret = null;
		URL lmsOutcomeServiceURL = null;
		String lmsResultSourcedId = null;
		SourcedIdPackage sourcedIdPackage = null;
		try
		{
			// break out the original LMS lis_result_sourcedid, LMS URL, key &
			// secret
			sourcedIdPackage = new GsonBuilder().create().fromJson(sourcedId, SourcedIdPackage.class);

			lmsoauthkey = sourcedIdPackage.getLmsOauthConsumerKey();

			LtiConsumer ltiConsumer = ltiConsumerService.findByConsumerKey(lmsoauthkey);
			if( ltiConsumer == null )
			{
				throw new ServletException("client not found for oauth key: " + lmsoauthkey);
			}
			clientSecret = encryptionService.decrypt(ltiConsumer.getConsumerSecret());

			lmsResultSourcedId = sourcedIdPackage.getLmsSourcedId();
			LOGGER.debug("Processing grades with JSON string " + lmsResultSourcedId + "...");
			try
			{
				lmsOutcomeServiceURL = new URL(sourcedIdPackage.getLmsOutcomeServiceUrl());
			}
			catch( MalformedURLException mal )
			{
				LOGGER.error(mal.getMessage(), mal);
				throw new WebException(400, OAuthConstants.ERROR_INVALID_REQUEST, mal.getMessage());
			}
			if( Check.isEmpty(lmsResultSourcedId) )
			{
				throw new ServletException("Unexpectedly empty sourcedId value in request");
			}
			env.getImsxPOXBody().getReplaceResultRequest().getResultRecord().getSourcedGUID()
				.setSourcedId(lmsResultSourcedId);
		}
		catch( JsonSyntaxException jse )
		{
			throw new ServletException("failed to break down lis_result_sourcedid data", jse);
		}

		LOGGER.debug("Sending grades in XML envelope " + env.toString() + "...");
		ltiService.sendGrade(env, lmsoauthkey, clientSecret, lmsOutcomeServiceURL.toExternalForm(), resp);
		LOGGER.debug("Concluded request to process grades...");
	}
}
