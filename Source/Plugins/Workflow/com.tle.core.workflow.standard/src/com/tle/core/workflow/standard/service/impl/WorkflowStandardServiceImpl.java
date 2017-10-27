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

package com.tle.core.workflow.standard.service.impl;

import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.tle.beans.IdTransformer;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.Check;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.events.GroupDeletedEvent;
import com.tle.core.events.GroupEditEvent;
import com.tle.core.events.GroupIdChangedEvent;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.GroupChangedListener;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.events.services.EventService;
import com.tle.core.guice.Bind;
import com.tle.core.item.event.ItemDeletedEvent;
import com.tle.core.item.event.ItemOperationEvent;
import com.tle.core.item.event.listener.ItemDeletedListener;
import com.tle.core.item.operations.BaseFilter;
import com.tle.core.item.standard.filter.workflow.ResetFilter;
import com.tle.core.plugins.AbstractFactoryLocator;
import com.tle.core.plugins.ClassBeanLocator;
import com.tle.core.workflow.dao.WorkflowDao;
import com.tle.core.workflow.event.WorkflowChangeEvent;
import com.tle.core.workflow.extension.WorkflowNodesSaveExtension;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.core.workflow.standard.filter.WorkflowStandardFilterFactory;
import com.tle.core.workflow.standard.service.WorkflowStandardService;

/**
 * @author Aaron
 */
@Bind(WorkflowStandardService.class)
@Singleton
public class WorkflowStandardServiceImpl
	implements
		WorkflowStandardService,
		WorkflowNodesSaveExtension,
		GroupChangedListener,
		UserChangeListener,
		ItemDeletedListener
{
	@Inject
	private EventService eventService;
	@Inject
	private WorkflowDao workflowDao;
	@Inject
	private WorkflowService workflowService;

	@Override
	public void workflowNodesSaved(Workflow oldWorkflow, Set<WorkflowNode> oldNodes, Set<WorkflowNode> deletedNodes,
		Set<WorkflowNode> changedNodes, Set<WorkflowNode> resaveNodes)
	{
		if( !deletedNodes.isEmpty() )
		{
			eventService.publishApplicationEvent(new WorkflowChangeEvent(oldWorkflow.getId(), deletedNodes, true));
			workflowDao.markForReset(deletedNodes);
			oldNodes.removeAll(deletedNodes);
			publishEventAfterCommit(new ItemOperationEvent(new ClassBeanLocator<ResetFilter>(ResetFilter.class)));
		}
		if( !changedNodes.isEmpty() )
		{
			publishEventAfterCommit(new ItemOperationEvent(new CheckForStepsLocator(changedNodes, false)));
		}
		if( !resaveNodes.isEmpty() )
		{
			publishEventAfterCommit(new ItemOperationEvent(new CheckForStepsLocator(resaveNodes, true)));
		}
	}

	@Override
	@Transactional
	public void groupEditedEvent(GroupEditEvent groupEditEvent)
	{
		groupChange(groupEditEvent.getGroupID());
	}

	private void groupChange(String groupId)
	{
		Collection<WorkflowItem> nodesForGroup = workflowDao.findTasksForGroup(groupId);

		if( !Check.isEmpty(nodesForGroup) )
		{
			publishEvent(new ItemOperationEvent(new CheckForStepsLocator(nodesForGroup, false)));
		}
	}

	@Override
	public void userEditedEvent(UserEditEvent event)
	{
		// Don't care
	}

	@Override
	@Transactional
	public void userDeletedEvent(UserDeletedEvent event)
	{
		Collection<WorkflowItem> nodesForUser = workflowDao.findTasksForUser(event.getUserID());
		if( !Check.isEmpty(nodesForUser) )
		{
			publishEvent(new ItemOperationEvent(new CheckForStepsLocator(nodesForUser, false)));
		}
	}

	@Override
	@Transactional
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		final String fromId = event.getFromUserId();
		final String toId = event.getToUserId();

		final Collection<WorkflowMessage> messages = workflowDao.findMessagesForUser(fromId);
		for( WorkflowMessage message : messages )
		{
			message.setUser(toId);
			workflowDao.saveAny(message);
		}

		final Collection<WorkflowItemStatus> wisses = workflowDao.findWorkflowItemStatusesForUser(fromId);
		for( WorkflowItemStatus wis : wisses )
		{
			if( wis.getAssignedTo() != null && wis.getAssignedTo().equals(fromId) )
			{
				wis.setAssignedTo(toId);
			}
			final Set<String> acceptedUsers = wis.getAcceptedUsers();
			if( acceptedUsers.contains(fromId) )
			{
				acceptedUsers.remove(fromId);
				acceptedUsers.add(toId);
			}
			workflowDao.saveAny(wis);
		}

		final Collection<ModerationStatus> mods = workflowDao.findModerationStatusesForUser(fromId);
		for( ModerationStatus mod : mods )
		{
			mod.setRejectedBy(toId);
			workflowDao.saveAny(mod);
		}

		Collection<WorkflowItem> nodesForUser = workflowDao.findTasksForUser(fromId);
		if( !Check.isEmpty(nodesForUser) )
		{
			for( WorkflowItem n : nodesForUser )
			{
				Set<String> users = n.getUsers();
				users.remove(fromId);
				users.add(toId);
			}

			publishEvent(new ItemOperationEvent(new CheckForStepsLocator(nodesForUser, false)));
		}
	}

	@Override
	@Transactional
	public void groupDeletedEvent(GroupDeletedEvent event)
	{
		groupChange(event.getGroupID());
	}

	@Override
	@Transactional
	public void groupIdChangedEvent(GroupIdChangedEvent event)
	{
		Collection<WorkflowItem> nodesForGroup = workflowDao.findTasksForGroup(event.getFromGroupId());
		if( !Check.isEmpty(nodesForGroup) )
		{
			for( WorkflowItem n : nodesForGroup )
			{
				Set<String> groups = n.getGroups();
				groups.remove(event.getFromGroupId());
				groups.add(event.getToGroupId());
			}

			publishEvent(new ItemOperationEvent(new CheckForStepsLocator(nodesForGroup, false)));
		}
	}

	private void publishEvent(ApplicationEvent<?> event)
	{
		eventService.publishApplicationEvent(event);
	}

	private void publishEventAfterCommit(final ApplicationEvent<?> event)
	{
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter()
		{
			@Override
			public void afterCommit()
			{
				eventService.publishApplicationEvent(event);
			}
		});
	}

	public static class CheckForStepsLocator extends AbstractFactoryLocator<WorkflowStandardFilterFactory, BaseFilter>
	{
		private static final long serialVersionUID = 1L;
		private final Collection<Long> nodeIds;
		private final boolean forceModify;

		public CheckForStepsLocator(Collection<? extends WorkflowNode> nodes, boolean forceModify)
		{
			super(WorkflowStandardFilterFactory.class);
			this.nodeIds = Lists.newArrayList(Collections2.transform(nodes, IdTransformer.INSTANCE));
			this.forceModify = forceModify;
		}

		@Override
		protected BaseFilter invoke(WorkflowStandardFilterFactory factory)
		{
			return factory.checkForSteps(nodeIds, forceModify);
		}
	}

	@Override
	@Transactional
	public void itemDeletedEvent(ItemDeletedEvent event)
	{
		ItemIdKey itemId = event.getItemId();
		workflowService.cleanupMessageFiles(itemId);
	}
}
