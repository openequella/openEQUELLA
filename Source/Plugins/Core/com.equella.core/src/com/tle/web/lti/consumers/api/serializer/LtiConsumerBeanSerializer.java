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

package com.tle.web.lti.consumers.api.serializer;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Strings;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.lti.consumers.LtiConsumerConstants.UnknownUser;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.lti.consumers.service.LtiConsumerService;
import com.tle.web.api.baseentity.serializer.AbstractEquellaBaseEntitySerializer;
import com.tle.web.api.users.interfaces.beans.GroupBean;
import com.tle.web.api.users.interfaces.beans.RoleBean;
import com.tle.web.lti.consumers.api.beans.LtiConsumerBean;
import com.tle.web.lti.consumers.api.serializer.LtiConsumerEditorImpl.LtiConsumerEditorFactory;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
@Singleton
public class LtiConsumerBeanSerializer
	extends
		AbstractEquellaBaseEntitySerializer<LtiConsumer, LtiConsumerBean, LtiConsumerEditor>
{
	@Inject
	private LtiConsumerService ltiConsumerService;
	@Inject
	private LtiConsumerEditorFactory editorFactory;
	@Inject
	private EncryptionService encryptionService;

	@Override
	protected LtiConsumerBean createBean()
	{
		return new LtiConsumerBean();
	}

	@Override
	protected LtiConsumer createEntity()
	{
		return new LtiConsumer();
	}

	@Override
	protected LtiConsumerEditor createExistingEditor(LtiConsumer entity, String stagingUuid, String lockId,
		boolean importing)
	{
		return editorFactory.createExistingEditor(entity, stagingUuid, lockId, true, importing);
	}

	@Override
	protected LtiConsumerEditor createNewEditor(LtiConsumer entity, String stagingUuid, boolean importing)
	{
		return editorFactory.createNewEditor(entity, stagingUuid, importing);
	}

	@Override
	protected void copyCustomFields(LtiConsumer entity, LtiConsumerBean bean, Object data)
	{
		bean.setConsumerKey(entity.getConsumerKey());
		bean.setConsumerSecret(encryptionService.decrypt(entity.getConsumerSecret()));
		bean.setAllowedUsersExpression(entity.getAllowedExpression());
		bean.setInstructorRoles(convertIdsToRoles(entity.getInstructorRoles()));
		bean.setOtherRoles(convertIdsToRoles(entity.getOtherRoles()));
		bean.setUnknownUserGroups(convertIdsToGroups(entity.getUnknownGroups()));
		bean.setUsernamePrefix(entity.getPrefix());
		bean.setUsernamePostfix(entity.getPostfix());

		final int unknownUserAction = entity.getUnknownUser();
		if( unknownUserAction == UnknownUser.CREATE.getValue() )
		{
			bean.setUnknownUserAction(LtiConsumerBean.ACTION_CREATE_USER);
		}
		else if( unknownUserAction == UnknownUser.DENY.getValue() )
		{
			bean.setUnknownUserAction(LtiConsumerBean.ACTION_ERROR);
		}
		else if( unknownUserAction == UnknownUser.IGNORE.getValue() )
		{
			bean.setUnknownUserAction(LtiConsumerBean.ACTION_GUEST);
		}
		else
		{
			bean.setUnknownUserAction(LtiConsumerBean.ACTION_ERROR);
		}
	}

	@Nullable
	private Set<RoleBean> convertIdsToRoles(@Nullable Set<String> ids)
	{
		if( ids == null )
		{
			return null;
		}
		final Set<RoleBean> beans = new HashSet<>();
		for( String id : ids )
		{
			if( !Strings.isNullOrEmpty(id) )
			{
				beans.add(new RoleBean(id));
			}
		}
		return beans;
	}

	@Nullable
	private Set<GroupBean> convertIdsToGroups(@Nullable Set<String> ids)
	{
		if( ids == null )
		{
			return null;
		}
		final Set<GroupBean> beans = new HashSet<>();
		for( String id : ids )
		{
			if( !Strings.isNullOrEmpty(id) )
			{
				beans.add(new GroupBean(id));
			}
		}
		return beans;
	}

	@Override
	protected AbstractEntityService<?, LtiConsumer> getEntityService()
	{
		return ltiConsumerService;
	}

	@Override
	protected Node getNonVirtualNode()
	{
		return Node.LTI_CONSUMER;
	}
}
