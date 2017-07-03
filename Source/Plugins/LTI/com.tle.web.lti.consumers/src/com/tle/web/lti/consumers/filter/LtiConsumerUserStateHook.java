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

package com.tle.web.lti.consumers.filter;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.dytech.edge.exceptions.WebException;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.lti.consumers.LtiConsumerConstants;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.lti.consumers.service.LtiConsumerService;
import com.tle.exceptions.AuthenticationException;
import com.tle.exceptions.UsernameNotFoundException;
import com.tle.web.core.filter.UserStateResult;
import com.tle.web.oauth.filter.AbstractOAuthV1UserStateHook;
import com.tle.web.sections.equella.annotation.PlugKey;

import net.oauth.OAuthMessage;
import net.oauth.server.OAuthServlet;

@Bind
@Singleton
public class LtiConsumerUserStateHook extends AbstractOAuthV1UserStateHook
{
	private static final Logger LOGGER = Logger.getLogger(LtiConsumerUserStateHook.class);

	@PlugKey("oauth.lti.error.")
	private static String LTI_ERROR_PREFIX;

	@Inject
	private LtiConsumerService ltiConsumerService;
	@Inject
	private EncryptionService encryptionService;

	@Override
	protected String getSecretFromKey(String consumerKey)
	{
		LtiConsumer consumer = ltiConsumerService.findByConsumerKey(consumerKey);
		if( consumer == null || consumer.isDisabled() )
		{
			String error = CurrentLocale
				.get(LTI_ERROR_PREFIX + (consumer == null ? "client.missing" : "client.disabled"));
			error += MessageFormat.format(" ({0} = {1})", LtiConsumerConstants.PARAM_CONSUMER_KEY, consumerKey);
			LOGGER.error(error);
			throw new WebException(500, error, error);
		}
		return encryptionService.decrypt(consumer.getConsumerSecret());
	}

	@Override
	protected UserStateResult getUserStateResult(HttpServletRequest request)
	{
		try
		{
			return super.getUserStateResult(request);
		}
		catch( UsernameNotFoundException e )
		{
			String error = e.getMessage();
			throw new WebException(401, error, CurrentLocale.get(LTI_ERROR_PREFIX + "nousername", e.getUsername()));
		}
		catch( AuthenticationException e )
		{
			throw new WebException(400, e.getMessage(), CurrentLocale.get(LTI_ERROR_PREFIX + e.getMessage()));
		}
	}

	/**
	 * The workaround for Moodle and Canvas OAuth.<br>
	 * If we have a duplicate, and it came from Moodle, it's worth presuming a
	 * different reality applies. Hopefully by version moodle-3 they'll have
	 * fixed this. ext_lms for moodle 2.3, 2.4, 2.5 was literally "moodle-2".
	 * Use startsWith in case future moodle 2.x has an extended string. Read:
	 * Dodgical hax
	 * 
	 * @param request
	 * @return
	 */
	@Override
	protected OAuthMessage getOAuthMessage(HttpServletRequest request)
	{
		boolean dupe = false;
		String extlms = request.getParameter(ExternalToolConstants.EXT_LMS);
		String product = request.getParameter(ExternalToolConstants.TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE);

		Set<Entry<String, String[]>> params = request.getParameterMap().entrySet();
		Map<String, String> newParams = Maps.newHashMap();

		if( "canvas".equalsIgnoreCase(product)
			|| (extlms != null && extlms.startsWith("moodle-2") && "moodle".equalsIgnoreCase(product))
			//hack for canvas ContentItemPlacements
			|| (request.getParameter("lti_message_type") != null
				&& request.getParameter("lti_message_type").equals("ContentItemSelectionRequest")) )
		{
			for( Entry<String, String[]> p : params )
			{
				String[] values = p.getValue();
				if( values.length == 2 && Objects.equal(values[0], values[1]) )
				{
					dupe = true;
				}
				newParams.put(p.getKey(), values[0]);
			}

			if( dupe )
			{
				return new OAuthMessage(request.getMethod(), urlService.getUriForRequest(request, null).toString(),
					newParams.entrySet());
			}
		}

		return OAuthServlet.getMessage(request, urlService.getUriForRequest(request, null).toString());
	}
}
