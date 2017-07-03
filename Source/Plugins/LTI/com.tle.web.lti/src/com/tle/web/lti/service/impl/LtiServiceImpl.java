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

package com.tle.web.lti.service.impl;

import java.io.StringReader;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import org.w3c.dom.Node;

import com.google.api.client.util.Base64;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.services.HttpService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Request.Method;
import com.tle.core.services.http.Response;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.xml.XmlDocument;
import com.tle.web.lti.LtiData;
import com.tle.web.lti.LtiData.LisData;
import com.tle.web.lti.LtiData.OAuthData;
import com.tle.web.lti.imsx.ImsxGWSVersionValueType;
import com.tle.web.lti.imsx.ImsxPOXBodyType;
import com.tle.web.lti.imsx.ImsxPOXEnvelopeType;
import com.tle.web.lti.imsx.ImsxPOXHeaderType;
import com.tle.web.lti.imsx.ImsxRequestHeaderInfoType;
import com.tle.web.lti.imsx.ObjectFactory;
import com.tle.web.lti.imsx.ReplaceResultRequest;
import com.tle.web.lti.imsx.ResultRecordType;
import com.tle.web.lti.imsx.ResultType;
import com.tle.web.lti.imsx.SourcedGUIDType;
import com.tle.web.lti.imsx.TextType;
import com.tle.web.lti.service.LtiService;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind(LtiService.class)
@Singleton
public class LtiServiceImpl implements LtiService
{
	@Inject
	private HttpService httpService;
	@Inject
	private ConfigurationService configService;

	@Override
	public void sendGrade(LtiData ltiData, String grade)
	{
		final LisData lisData = ltiData.getLisData();
		final String outcomeServiceUrl = lisData.getOutcomeServiceUrl();

		if( outcomeServiceUrl != null )
		{
			try
			{
				final OAuthData oauthData = ltiData.getOAuthData();
				final Request req = createLtiOutcomesRequest(buildEnvelope(lisData, grade), outcomeServiceUrl,
					ltiData.getOAuthData().getConsumerKey(), oauthData.getConsumerSecret());
				try( Response resp = httpService.getWebContent(req, configService.getProxyDetails()) )
				{
					parseError(resp, outcomeServiceUrl);
				}
			}
			catch( Exception e )
			{
				throw Throwables.propagate(e);
			}
		}
	}

	@Override
	public void sendGrade(ImsxPOXEnvelopeType envelope, String consumerKey, String secret, String url,
		HttpServletResponse servletResponse)
	{
		if( url != null )
		{
			try
			{
				final Request req = createLtiOutcomesRequest(envelope, url, consumerKey, secret);
				try( Response httpResp = httpService.getWebContent(req, configService.getProxyDetails()) )
				{
					parseError(httpResp, url);
					String respBody = httpResp.getBody();
					final String bodyHash = calcSha1Hash(respBody);
					final OAuthMessage message = createLaunchParameters(consumerKey, secret, url, bodyHash);

					servletResponse.addHeader("Authorization", message.getAuthorizationHeader(""));
					CharStreams.copy(new StringReader(httpResp.getBody()), servletResponse.getWriter());
				}
			}
			catch( Exception e )
			{
				throw Throwables.propagate(e);
			}
		}
	}

	private void parseError(Response resp, String url) throws Exception
	{
		// BB returns 200 for errors anyway, need to parse the
		// actual response
		if( !httpService.isError(resp) )
		{
			final String body = resp.getBody();

			// Dirty hack for Moodle. Can't use deserialiser because of weird
			// IMS OMS spec mismatches.
			final XmlDocument xml = new XmlDocument(body);
			String codeMajor = "No imsxCodeMajor";
			String description = "";

			final Node statusInfoNode = xml
				.node("/imsx_POXEnvelopeResponse/imsx_POXHeader/imsx_POXResponseHeaderInfo/imsx_statusInfo");
			if( statusInfoNode != null )
			{
				String cm = xml.nodeValue("imsx_codeMajor", statusInfoNode);
				if( cm != null )
				{
					codeMajor = cm.toLowerCase();
				}
				String desc = xml.nodeValue("imsx_description", statusInfoNode);
				if( desc != null )
				{
					description = desc;
				}
			}

			if( !codeMajor.equals("success") )
			{
				throw new RuntimeException("Error returned from gradebook (" + codeMajor + ") " + description);
			}
		}
		else
		{
			// If something has gone wrong n sending to the LMS, any details we
			// can extract could be valuable ...
			String errMsg = resp.getCode() + " error returned from gradebook.\n";
			if( !Check.isEmpty(resp.getMessage()) )
			{
				errMsg += resp.getMessage();
			}
			else
			{
				errMsg += "(no message in " + resp.getCode() + " error)";
			}
			errMsg += "\nAddressed to URL: " + url;
			throw new RuntimeException(errMsg);
		}
	}

	private ImsxPOXEnvelopeType buildEnvelope(LisData lisData, String gradeString)
	{
		final ImsxPOXEnvelopeType imsxEnvelope = new ImsxPOXEnvelopeType();

		final ImsxPOXHeaderType imsxHeader = new ImsxPOXHeaderType();
		imsxEnvelope.setImsxPOXHeader(imsxHeader);

		final ImsxPOXBodyType imsxBody = new ImsxPOXBodyType();
		imsxEnvelope.setImsxPOXBody(imsxBody);

		final ReplaceResultRequest imsxReplaceResult = new ReplaceResultRequest();
		imsxBody.setReplaceResultRequest(imsxReplaceResult);

		final ImsxRequestHeaderInfoType imsxHeaderInfo = new ImsxRequestHeaderInfoType();
		imsxHeaderInfo.setImsxVersion(ImsxGWSVersionValueType.V_1_0);
		imsxHeaderInfo.setImsxMessageIdentifier(UUID.randomUUID().toString());
		imsxHeader.setImsxPOXRequestHeaderInfo(imsxHeaderInfo);

		final ResultRecordType imsxResult = new ResultRecordType();
		final SourcedGUIDType value = new SourcedGUIDType();
		imsxResult.setSourcedGUID(value);
		value.setSourcedId(lisData.getResultSourcedid());
		imsxReplaceResult.setResultRecord(imsxResult);

		final ResultType resultType = new ResultType();
		imsxResult.setResult(resultType);

		final TextType resultValue = new TextType();
		resultValue.setTextString(gradeString);
		resultValue.setLanguage(CurrentLocale.getLocale().toString());
		resultType.setResultScore(resultValue);

		return imsxEnvelope;
	}

	// Build the web request using OAuth.Net to build the Authorization header
	private Request createLtiOutcomesRequest(ImsxPOXEnvelopeType imsxEnvelope, String url, String consumerKey,
		String consumerSecret) throws Exception
	{
		final Request webRequest = new Request(url);
		webRequest.setMethod(Method.POST);
		webRequest.setMimeType("application/xml");

		final ObjectFactory of = new ObjectFactory();
		final JAXBElement<ImsxPOXEnvelopeType> createImsxPOXEnvelopeRequest = of
			.createImsxPOXEnvelopeRequest(imsxEnvelope);

		final JAXBContext jc = JAXBContext.newInstance("com.tle.web.lti.imsx", LtiServiceImpl.class.getClassLoader());
		final Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		final StringWriter sw = new StringWriter();
		marshaller.marshal(createImsxPOXEnvelopeRequest, sw);
		webRequest.setBody(sw.toString());

		final String bodyHash = calcSha1Hash(webRequest.getBody());
		final OAuthMessage message = createLaunchParameters(consumerKey, consumerSecret, url, bodyHash);
		webRequest.addHeader("Authorization", message.getAuthorizationHeader(""));
		return webRequest;
	}

	private static String calcSha1Hash(String data)
	{
		try
		{
			final MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(data.getBytes("UTF-8"));
			byte[] rawHmac = crypt.digest();
			return Base64.encodeBase64String(rawHmac);
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	private OAuthMessage createLaunchParameters(String consumerKey, String secret, String url, String bodyHash)
	{
		final String nonce = UUID.randomUUID().toString();
		final String timestamp = Long.toString(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

		final OAuthMessage message = new OAuthMessage(OAuthMessage.POST, url, null);
		message.addParameter(OAuth.OAUTH_CONSUMER_KEY, consumerKey);
		message.addParameter(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
		message.addParameter(OAuth.OAUTH_NONCE, nonce);
		message.addParameter(OAuth.OAUTH_TIMESTAMP, timestamp);
		message.addParameter("oauth_body_hash", bodyHash);

		final OAuthConsumer consumer = new OAuthConsumer(null, consumerKey, secret, null);
		final OAuthAccessor accessor = new OAuthAccessor(consumer);
		try
		{
			message.sign(accessor);
			return message;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}
}
