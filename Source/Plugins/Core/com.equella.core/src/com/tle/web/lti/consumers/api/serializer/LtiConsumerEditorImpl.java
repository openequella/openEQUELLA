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

import com.google.common.base.Strings;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.lti.consumers.LtiConsumerConstants.UnknownUser;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.BindFactory;
import com.tle.core.lti.consumers.service.LtiConsumerService;
import com.tle.web.api.baseentity.serializer.AbstractBaseEntityEditor;
import com.tle.web.api.users.interfaces.beans.GroupBean;
import com.tle.web.api.users.interfaces.beans.RoleBean;
import com.tle.web.lti.consumers.api.beans.LtiConsumerBean;

/**
 * @author Aaron
 */
@NonNullByDefault
public class LtiConsumerEditorImpl extends AbstractBaseEntityEditor<LtiConsumer, LtiConsumerBean>
	implements
		LtiConsumerEditor
{
	@Inject
	private LtiConsumerService ltiConsumerService;

	@AssistedInject
	public LtiConsumerEditorImpl(@Assisted LtiConsumer entity, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("lockId") @Nullable String lockId, @Assisted("editing") boolean editing,
		@Assisted("importing") boolean importing)
	{
		super(entity, stagingUuid, lockId, editing, importing);
	}

	@AssistedInject
	public LtiConsumerEditorImpl(@Assisted LtiConsumer entity, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("importing") boolean importing)
	{
		this(entity, stagingUuid, null, false, importing);
	}

	@Override
	protected void copyCustomFields(LtiConsumerBean bean)
	{
		super.copyCustomFields(bean);

		entity.setConsumerKey(bean.getConsumerKey());
		entity.setConsumerSecret(bean.getConsumerSecret());
		entity.setAllowedExpression(bean.getAllowedUsersExpression());
		entity.setInstructorRoles(convertRolesToIds(bean.getInstructorRoles()));
		entity.setOtherRoles(convertRolesToIds(bean.getOtherRoles()));
		entity.setUnknownGroups(convertGroupsToIds(bean.getUnknownUserGroups()));
		entity.setPrefix(bean.getUsernamePrefix());
		entity.setPostfix(bean.getUsernamePostfix());

		final String unknownUserAction = bean.getUnknownUserAction();
		if( !Strings.isNullOrEmpty(unknownUserAction) )
		{
			switch( unknownUserAction )
			{
				case LtiConsumerBean.ACTION_CREATE_USER:
					entity.setUnknownUser(UnknownUser.CREATE.getValue());
					break;
				case LtiConsumerBean.ACTION_ERROR:
					entity.setUnknownUser(UnknownUser.DENY.getValue());
					break;
				case LtiConsumerBean.ACTION_GUEST:
					entity.setUnknownUser(UnknownUser.IGNORE.getValue());
					break;
				default:
					entity.setUnknownUser(UnknownUser.DENY.getValue());
			}
		}
		else
		{
			entity.setUnknownUser(UnknownUser.DENY.getValue());
		}
	}

	@Nullable
	private Set<String> convertRolesToIds(@Nullable Set<RoleBean> roleBeans)
	{
		if( roleBeans == null )
		{
			return null;
		}
		final Set<String> ids = new HashSet<>();
		for( final RoleBean rb : roleBeans )
		{
			final String id = rb.getId();
			if( !Strings.isNullOrEmpty(id) )
			{
				ids.add(id);
			}
		}
		return ids;
	}

	@Nullable
	private Set<String> convertGroupsToIds(@Nullable Set<GroupBean> groupBeans)
	{
		if( groupBeans == null )
		{
			return null;
		}
		final Set<String> ids = new HashSet<>();
		for( final GroupBean gb : groupBeans )
		{
			final String id = gb.getId();
			if( !Strings.isNullOrEmpty(id) )
			{
				ids.add(id);
			}
		}
		return ids;
	}

	@Override
	protected void afterFinishedEditing()
	{
		super.afterFinishedEditing();
	}

	@Override
	protected AbstractEntityService<?, LtiConsumer> getEntityService()
	{
		return ltiConsumerService;
	}

	@BindFactory
	public interface LtiConsumerEditorFactory
	{
		@Nullable
		LtiConsumerEditorImpl createExistingEditor(@Assisted LtiConsumer entity,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
			@Assisted("editing") boolean editing, @Assisted("importing") boolean importing);

		LtiConsumerEditorImpl createNewEditor(@Assisted LtiConsumer entity,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("importing") boolean importing);
	}
}
