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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.transaction.annotation.Transactional;

import com.dytech.devlib.Md5;
import com.google.api.client.util.Sets;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.user.TLEUser;
import com.tle.common.Check;
import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.lti.consumers.LtiConsumerConstants;
import com.tle.common.lti.consumers.LtiConsumerConstants.UnknownUser;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.common.lti.consumers.entity.LtiConsumerCustomRole;
import com.tle.common.usermanagement.user.AnonymousUserState;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.common.usermanagement.user.valuebean.DefaultUserBean;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.lti.consumers.service.LtiConsumerService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.impl.AclExpressionEvaluator;
import com.tle.core.usermanagement.standard.service.TLEGroupService;
import com.tle.core.usermanagement.standard.service.TLEUserService;
import com.tle.exceptions.AuthenticationException;
import com.tle.exceptions.UsernameNotFoundException;
import com.tle.plugins.ump.AbstractUserDirectory;
import com.tle.web.lti.LtiData;
import com.tle.web.lti.LtiData.LisData;
import com.tle.web.lti.LtiData.OAuthData;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class LtiWrapper extends AbstractUserDirectory
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(LtiWrapper.class);

	@PlugKey("wrapper.error.expression")
	private static Label expressionError;

	@Inject
	private RunAsInstitution runAs;
	@Inject
	private TLEUserService tleUserService;
	@Inject
	private TLEGroupService tleGroupService;
	@Inject
	private PluginTracker<LtiWrapperExtension> extensions;
	@Inject
	private LtiConsumerService consumerService;
	@Inject
	private EncryptionService encryptionService;

	@Override
	protected boolean initialise(UserManagementSettings settings)
	{
		return false;
	}

	@Nullable
	@Override
	@Transactional
	public ModifiableUserState authenticateRequest(HttpServletRequest request)
	{
		// Check if it has required LTI parameters
		String ltiVersion = request.getParameter(ExternalToolConstants.LTI_VERSION);
		String ltiMessageType = request.getParameter(ExternalToolConstants.LTI_MESSAGE_TYPE);
		Set<String> createGroups = Sets.newHashSet();
		boolean userCreated = false;

		if( !Check.isEmpty(ltiVersion) && !Check.isEmpty(ltiMessageType) )
		{
			LtiConsumer consumer = consumerService
				.findByConsumerKey(request.getParameter(LtiConsumerConstants.PARAM_CONSUMER_KEY));

			ModifiableUserState userState = null;
			final String username = getUsername(request, null, consumer);
			if( username != null )
			{
				userState = getChain().authenticateUserFromUsername(username, null);
				if( userState == null )
				{
					if( consumer.getUnknownUser() == UnknownUser.CREATE.getValue() )
					{
						userCreated = createOrEditUser(request, username);
						userState = getChain().authenticateUserFromUsername(username, null);
						if( userCreated )
						{
							createGroups = consumer.getUnknownGroups();
							if( !Check.isEmpty(createGroups) )
							{
								userState.getUsersGroups().addAll(createGroups);
							}
						}
					}
					else if( consumer.getUnknownUser() == UnknownUser.IGNORE.getValue() )
					{
						userState = new AnonymousUserState();
					}
					else
					{
						throw new UsernameNotFoundException(username);
					}
				}
			}

			final LtiUserState ltiUserState;
			if( userState == null )
			{
				ltiUserState = new LtiUserState();
			}
			else
			{
				ltiUserState = new LtiUserState(userState);
			}
			ltiUserState.setData(generateLtiData(request));
			ltiUserState.getUsersRoles().addAll(getDynamicRoles(request, consumer));
			if( ltiUserState.getUserBean() == null )
			{
				setupUser(request, ltiUserState);
			}

			if( userCreated && !Check.isEmpty(createGroups) )
			{
				final List<String> groupIds = Lists.newArrayList(createGroups);
				runAs.executeAsSystem(CurrentInstitution.get(), new Runnable()
				{
					@Override
					public void run()
					{
						for( String groupId : groupIds )
						{
							tleGroupService.addUserToGroup(groupId, getUserId(request));
						}
					}
				});
			}

			AclExpressionEvaluator evaluator = new AclExpressionEvaluator();
			if( !evaluator.evaluate(consumer.getAllowedExpression(), ltiUserState, false) )
			{
				throw new AuthenticationException(expressionError.getText());
			}

			return ltiUserState;
		}

		return null;
	}

	@Nullable
	private String getUsername(HttpServletRequest request, @Nullable String fallbackUsername,
		@Nullable LtiConsumer consumer)
	{
		String username = fallbackUsername;
		for( LtiWrapperExtension extension : extensions.getBeanList() )
		{
			final String extensionUsername = extension.getUsername(request);
			if( !Strings.isNullOrEmpty(extensionUsername) )
			{
				username = extensionUsername;
				break;
			}
		}
		if( consumer != null )
		{
			if( !Strings.isNullOrEmpty(consumer.getPrefix()) )
			{
				username = consumer.getPrefix() + username;
			}
			if( !Strings.isNullOrEmpty(consumer.getPostfix()) )
			{
				username += consumer.getPostfix();
			}
		}
		return username;

	}

	private String getUserId(HttpServletRequest request)
	{
		String userId = null;
		boolean prefix = true;
		for( LtiWrapperExtension extension : extensions.getBeanList() )
		{
			userId = extension.getUserId(request);
			if( userId != null )
			{
				prefix = extension.isPrefixUserId();
				break;
			}
		}
		if( prefix )
		{
			final String consumerInstanceId = request.getParameter(ExternalToolConstants.TOOL_CONSUMER_INSTANCE_GUID);
			if( consumerInstanceId != null )
			{
				if( userId == null )
				{
					userId = request.getParameter(ExternalToolConstants.USER_ID);
				}
				if( userId != null )
				{
					// The code below is an attempt to have a unique ID for any
					// given connected LMS+user.
					// Typically Moodle user IDs are "1" or "2" etc., hence the need
					// for the concatenation.

					// The Md5 hashing is an attempt to prevent the user ID going
					// past 40 chars.
					// (substring of the Md5 hash to make it smaller, and more
					// likely to be unique than a straight substring
					// of the consumer instance guid)
					final String consumerHash = new Md5(consumerInstanceId).getStringDigest().substring(0, 10);
					final String finalId = consumerHash + "_" + userId;
					return finalId;
				}
			}
		}
		if( userId == null )
		{
			return UUID.randomUUID().toString();
		}
		return userId;
	}

	private String getFirstName(HttpServletRequest request)
	{
		for( LtiWrapperExtension extension : extensions.getBeanList() )
		{
			final String givenName = extension.getFirstName(request);
			if( !Strings.isNullOrEmpty(givenName) )
			{
				return givenName;
			}
		}
		return resources.getString("wrapper.userdefaults.givenname");
	}

	private String getLastName(HttpServletRequest request)
	{
		for( LtiWrapperExtension extension : extensions.getBeanList() )
		{
			final String lastName = extension.getLastName(request);
			if( !Strings.isNullOrEmpty(lastName) )
			{
				return lastName;
			}
		}
		return resources.getString("wrapper.userdefaults.familyname");
	}

	@Nullable
	private String getEmail(HttpServletRequest request)
	{
		for( LtiWrapperExtension extension : extensions.getBeanList() )
		{
			final String email = extension.getEmail(request);
			if( email != null )
			{
				return email;
			}
		}
		return null;
	}

	private boolean createOrEditUser(HttpServletRequest request, String username)
	{
		String userId = getUserId(request);
		TLEUser tleUser = tleUserService.get(userId);
		if( tleUser != null )
		{
			final TLEUser updateUser = tleUser;
			updateUser.setUsername(username);
			runAs.executeAsSystem(CurrentInstitution.get(), new Runnable()
			{
				@Override
				public void run()
				{
					tleUserService.edit(updateUser, false);
				}
			});
			return false;
		}

		final TLEUser user = new TLEUser();
		user.setUuid(userId);
		user.setUsername(username);
		user.setFirstName(getFirstName(request));
		user.setLastName(getLastName(request));
		user.setEmailAddress(getEmail(request));
		user.setPassword(UUID.randomUUID().toString());

		runAs.executeAsSystem(CurrentInstitution.get(), new Runnable()
		{
			@Override
			public void run()
			{
				tleUserService.add(user);
			}
		});
		return true;

	}

	private Collection<String> getDynamicRoles(HttpServletRequest request, LtiConsumer consumer)
	{
		final String roles = request.getParameter("roles");
		final List<String> ltiRoles = roles == null ? null : Lists.newArrayList(Splitter.on(",").split(roles));

		if( ltiRoles == null )
		{
			return Collections.<String>emptyList();
		}

		final Set<String> extraRoles = new HashSet<String>();

		if( !Collections.disjoint(ltiRoles, ExternalToolConstants.INSTRUCTOR_ROLES_SET) )
		{
			final Set<String> instructorRoles = consumer.getInstructorRoles();
			if( !Check.isEmpty(instructorRoles) )
			{
				extraRoles.addAll(instructorRoles);
			}
		}

		if( !Check.isEmpty(consumer.getCustomRoles()) )
		{
			Set<LtiConsumerCustomRole> customRoles = consumer.getCustomRoles();
			for( LtiConsumerCustomRole custom : customRoles )
			{
				String matcher = custom.getLtiRole();
				if( ltiRoles.contains(matcher) || ltiRoles.contains(ExternalToolConstants.URN + matcher)
					|| ltiRoles.contains(ExternalToolConstants.CONTEXT_URN + matcher) )
				{
					extraRoles.add(custom.getEquellaRole());
				}
			}
		}
		// no matches yet
		if( extraRoles.isEmpty() )
		{
			final Set<String> otherRoles = consumer.getOtherRoles();
			if( !Check.isEmpty(otherRoles) )
			{
				extraRoles.addAll(otherRoles);
			}
		}

		return extraRoles;
	}

	private void setupUser(HttpServletRequest request, ModifiableUserState state)
	{
		final String userId = getUserId(request);
		state.setLoggedInUser(new DefaultUserBean(userId, getUsername(request, "lti:" + userId, null),
			getFirstName(request), getLastName(request), getEmail(request)));
	}

	private LtiData generateLtiData(HttpServletRequest request)
	{
		final String ltiConsumerKey = request.getParameter(LtiConsumerConstants.PARAM_CONSUMER_KEY);
		final LtiConsumer consumer = consumerService.findByConsumerKey(ltiConsumerKey);

		final LtiData ltiData = new LtiData();
		ltiData.setContextId(request.getParameter("context_id"));
		ltiData.setContextLabel(request.getParameter("context_label"));
		ltiData.setContextTitle(request.getParameter("context_title"));
		ltiData.setUserId(request.getParameter("user_id"));
		ltiData.setResourceLinkId(request.getParameter("resource_link_id"));
		ltiData.setResourceLinkTitle(request.getParameter("resource_link_title"));
		ltiData.setToolConsumerInfoProductFamilyCode(request.getParameter("tool_consumer_info_product_family_code"));
		ltiData.setToolConsumerInfoVersion(request.getParameter("tool_consumer_info_version"));
		ltiData.setToolConsumerInstanceGuid(request.getParameter("tool_consumer_instance_guid"));
		ltiData.setReturnUrl(request.getParameter("launch_presentation_return_url"));
		ltiData.setLaunchPresentationDocumentTarget(request.getParameter("launch_presentation_document_target"));

		// Custom lti params
		final Map<String, String[]> paramMap = request.getParameterMap();
		for( Entry<String, String[]> entry : paramMap.entrySet() )
		{
			final String[] value = entry.getValue();
			if( value != null && value.length > 0 )
			{
				ltiData.addCustom(entry.getKey(), value[0]);
			}
		}

		final OAuthData oData = new OAuthData();
		oData.setNonce(request.getParameter("oauth_nonce"));
		oData.setConsumerKey(request.getParameter("oauth_consumer_key"));
		oData.setConsumerSecret(consumer == null ? null : encryptionService.decrypt(consumer.getConsumerSecret()));
		oData.setSignatureMethod(request.getParameter("oauth_signature_method"));
		ltiData.setOAuthData(oData);

		final LisData lData = new LisData();
		lData.setOutcomeServiceUrl(request.getParameter("lis_outcome_service_url"));
		lData.setResultSourcedid(request.getParameter("lis_result_sourcedid"));
		lData.setPersonNameFamily(request.getParameter("lis_person_name_family"));
		lData.setPersonNameGiven(request.getParameter("lis_person_name_given"));
		lData.setPersonNameFull(request.getParameter("lis_person_name_full"));
		lData.setContactEmailPrimary(request.getParameter("lis_person_contact_email_primary"));
		ltiData.setLisData(lData);

		return ltiData;
	}

	@Override
	public void initUserState(ModifiableUserState state)
	{
		// No
	}
}
