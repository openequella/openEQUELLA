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
import com.tle.core.guice.Bind;
import com.tle.core.lti.consumers.service.LtiConsumerService;
import com.tle.core.services.entity.AbstractEntityService;
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
		bean.setConsumerSecret(entity.getConsumerSecret());
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
