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

package com.tle.core.lti.consumers.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.tle.common.beans.exception.ValidationError;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.Pair;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.common.lti.consumers.entity.LtiConsumerCustomRole;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.guice.Bind;
import com.tle.core.lti.consumers.dao.LtiConsumerDao;
import com.tle.core.lti.consumers.service.LtiConsumerService;
import com.tle.core.lti.consumers.service.session.LtiConsumerEditingBean;
import com.tle.core.lti.consumers.service.session.LtiConsumerEditingSession;
import com.tle.core.security.impl.SecureEntity;

@NonNullByDefault
@Bind(LtiConsumerService.class)
@Singleton
@SecureEntity(LtiConsumerService.ENTITY_TYPE)
public class LtiConsumerServiceImpl
	extends
		AbstractEntityServiceImpl<LtiConsumerEditingBean, LtiConsumer, LtiConsumerService>
	implements LtiConsumerService
{
	@Inject
	private EncryptionService encryptionService;

	LtiConsumerDao dao;

	@Inject
	public LtiConsumerServiceImpl(LtiConsumerDao ltiConsumerDao)
	{
		super(Node.LTI_CONSUMER, ltiConsumerDao);
		dao = ltiConsumerDao;
	}

	@Override
	protected void doValidation(@Nullable EntityEditingSession<LtiConsumerEditingBean, LtiConsumer> session,
		LtiConsumer lc, List<ValidationError> errors)
	{
		// nothing
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <SESSION extends EntityEditingSession<LtiConsumerEditingBean, LtiConsumer>> SESSION createSession(
		String sessionId, EntityPack<LtiConsumer> pack, LtiConsumerEditingBean bean)
	{
		return (SESSION) new LtiConsumerEditingSession(sessionId, pack, bean);
	}

	@Override
	protected LtiConsumerEditingBean createEditingBean()
	{
		return new LtiConsumerEditingBean();
	}

	@Override
	protected boolean isUseEditingBean()
	{
		return true;
	}

	@Override
	protected void populateEditingBean(LtiConsumerEditingBean bean, LtiConsumer entity)
	{
		super.populateEditingBean(bean, entity);

		if( bean.getId() != 0 )
		{
			bean.setConsumerKey(entity.getConsumerKey());
			bean.setConsumerSecret(encryptionService.decrypt(entity.getConsumerSecret()));
			bean.setPrefix(entity.getPrefix());
			bean.setPostfix(entity.getPostfix());
			bean.setAllowedExpression(entity.getAllowedExpression());
			bean.setInstructorRoles(copySet(entity.getInstructorRoles()));
			bean.setOtherRoles(copySet(entity.getOtherRoles()));
			bean.setUnknownUser(entity.getUnknownUser());
			bean.setUnknownGroups(copySet(entity.getUnknownGroups()));

			final Set<Pair<String, String>> customRoleBeans = new HashSet<Pair<String, String>>();
			entity.getCustomRoles()
				.forEach(cr -> customRoleBeans.add(new Pair<String, String>(cr.getLtiRole(), cr.getEquellaRole())));
			bean.setCustomRoles(customRoleBeans);
		}
	}

	@Override
	protected void populateEntity(LtiConsumerEditingBean bean, LtiConsumer entity)
	{
		super.populateEntity(bean, entity);
		entity.setConsumerKey(bean.getConsumerKey());
		entity.setConsumerSecret(encryptionService.encrypt(bean.getConsumerSecret()));
		entity.setPrefix(bean.getPrefix());
		entity.setPostfix(bean.getPostfix());
		entity.setAllowedExpression(bean.getAllowedExpression());
		entity.setInstructorRoles(copySet(bean.getInstructorRoles()));
		entity.setOtherRoles(copySet(bean.getOtherRoles()));
		entity.setUnknownUser(bean.getUnknownUser());
		entity.setUnknownGroups(copySet(bean.getUnknownGroups()));

		entity.getCustomRoles().clear();
		final Set<Pair<String, String>> customRoleBeans = bean.getCustomRoles();
		if( !Check.isEmpty(customRoleBeans) )
		{
			final Set<LtiConsumerCustomRole> customRoles = new HashSet<LtiConsumerCustomRole>();
			for( Pair<String, String> customRoleBean : customRoleBeans )
			{
				final LtiConsumerCustomRole role = new LtiConsumerCustomRole();
				role.setLtiRole(customRoleBean.getFirst());
				role.setEquellaRole(customRoleBean.getSecond());
				role.setConsumer(entity);
				customRoles.add(role);
			}
			entity.getCustomRoles().addAll(customRoles);
		}
	}

	private Set<String> copySet(Set<String> property)
	{
		if( !Check.isEmpty(property) )
		{
			return Sets.newHashSet(property);
		}
		return null;
	}

	@Override
	public LtiConsumer findByConsumerKey(String consumerKey)
	{
		return dao.findByConsumerKey(consumerKey);
	}
}
