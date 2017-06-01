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

package com.tle.core.workflow.operations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.common.valuebean.UserBean;
import com.dytech.edge.exceptions.WorkflowException;
import com.google.common.collect.Sets;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.notification.NotificationService;
import com.tle.core.notification.beans.Notification;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.TaskStatisticsService;
import com.tle.core.services.WorkflowOperationService;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.core.services.item.ItemService;
import com.tle.core.services.user.UserService;
import com.tle.core.user.CurrentUser;

/**
 * @author jmaginnis
 */
public abstract class AbstractWorkflowOperation implements WorkflowOperation
{
	protected static final Logger LOGGER = Logger.getLogger(AbstractWorkflowOperation.class);

	@Inject
	protected ItemDefinitionService itemdefService;
	@Inject
	protected ItemService itemService;
	@Inject
	protected UserService userService;
	@Inject
	protected FileSystemService fileSystemService;
	@Inject
	protected WorkflowOperationService workflowOpService;
	@Inject
	protected NotificationService notificationService;
	@Inject
	private TaskStatisticsService taskStatsService;

	protected WorkflowParams params;

	private boolean injected;

	@Override
	public void setParams(WorkflowParams params)
	{
		this.params = params;
	}

	public String getId()
	{
		return getItem().getUuid();
	}

	public int getVersion()
	{
		return getItem().getVersion();
	}

	public ItemStatus getItemStatus()
	{
		return getItem().getStatus();
	}

	public ItemDefinition getItemdef()
	{
		return getItem().getItemDefinition();
	}

	public Schema getSchema()
	{
		return getItemdef().getSchema();
	}

	protected String getItemOwnerId()
	{
		return getItem().getOwner();
	}

	protected Collection<String> getAllOwnerIds()
	{
		Set<String> owners = Sets.newHashSet();
		owners.add(getItemOwnerId());
		owners.addAll(getItem().getCollaborators());
		return owners;
	}

	public Workflow getWorkflow()
	{
		return getItemdef().getWorkflow();
	}

	protected HistoryEvent createHistory(Type type)
	{
		HistoryEvent historyEvent = new HistoryEvent(type, getItem());
		historyEvent.setUser(getUserId());
		historyEvent.setDate(params.getDateNow());
		List<HistoryEvent> history = getItem().getHistory();
		history.add(historyEvent);
		return historyEvent;
	}

	public boolean setState(ItemStatus state)
	{
		Item itembean = getItem();
		ItemStatus oldState = itembean.getStatus();
		itembean.setStatus(state);
		if( !Objects.equals(oldState, state) )
		{
			params.setUpdateSecurity(true);
			params.setRequiresReindex(true);
			if( oldState != null )
			{
				if( oldState == ItemStatus.REJECTED )
				{
					removeNotificationsForItem(getItemId(), Notification.REASON_REJECTED);
				}
				else if( oldState == ItemStatus.LIVE )
				{
					removeNotificationsForItem(getItemId(), Notification.REASON_WENTLIVE, Notification.REASON_WENTLIVE2);
				}
				HistoryEvent history = createHistory(Type.statechange);
				history.setState(state);
			}
			return true;
		}
		return false;
	}

	protected boolean checkStatus(ItemStatus status)
	{
		ItemStatus curstatus = getItem().getStatus();
		return curstatus.equals(status);
	}

	protected Date calculateReviewDate()
	{
		ItemDefinition myItemDef = getItemdef();
		ModerationStatus status = getModerationStatus();

		if( myItemDef.hasReviewPeriod() )
		{
			int lReviewPeriod = myItemDef.getReviewperiod();
			Date review = null;
			// We get problems if review date is < 0
			if( lReviewPeriod >= 0 )
			{
				Calendar oTimeStamp = Calendar.getInstance();
				oTimeStamp.add(Calendar.DAY_OF_YEAR, lReviewPeriod);
				review = oTimeStamp.getTime();
				status.setReviewDate(review);
			}
			return review;
		}
		else
		{
			status.setReviewDate(null);
			return null;
		}
	}

	public ModerationStatus getModerationStatus()
	{
		Item item = getItem();
		ModerationStatus status = item.getModeration();
		if( status == null )
		{
			status = new ModerationStatus();
			item.setModeration(status);
		}
		return status;
	}

	public void makeLive(boolean finishedModerating)
	{
		if( finishedModerating )
		{
			getItem().setModerating(false);
			calculateReviewDate();
		}
		if( setState(ItemStatus.LIVE) )
		{
			params.setWentLive(true);
			ModerationStatus status = getModerationStatus();
			status.setLiveApprovalDate(params.getDateNow());
		}
	}

	public void enterTask(final WorkflowItem task)
	{
		if( params.isUpdate() )
		{
			taskStatsService.enterTask(getItem(), task, params.getDateNow());
		}
		else
		{
			params.addAfterSaveHook(new Runnable()
			{

				@Override
				public void run()
				{
					taskStatsService.enterTask(getItem(), task, params.getDateNow());
				}
			});
		}
	}

	public void exitTask(final WorkflowItem task)
	{
		if( params.isUpdate() )
		{
			taskStatsService.exitTask(getItem(), task, params.getDateNow());
		}
		else
		{
			params.addAfterSaveHook(new Runnable()
			{

				@Override
				public void run()
				{
					taskStatsService.exitTask(getItem(), task, params.getDateNow());
				}
			});
		}
	}

	public void exitTasksForItem()
	{
		if( params.isUpdate() )
		{
			taskStatsService.exitAllTasksForItem(getItem(), params.getDateNow());
		}
	}

	public void restoreTasksForItem()
	{
		Item item = getItem();
		if( item.isModerating() )
		{
			taskStatsService.restoreTasksForItem(item);
		}
	}

	@Nullable
	@Override
	public Item getItem()
	{
		final ItemPack<Item> pack = params.getItemPack();
		if( pack != null )
		{
			return pack.getItem();
		}
		return null;
	}

	public Attachments getAttachments()
	{
		return new UnmodifiableAttachments(getItem());
	}

	public List<String> getUserIdsInGroup(String group)
	{
		try
		{
			List<String> userIds = new ArrayList<String>();
			Collection<UserBean> usersForGroup = userService.getUsersInGroup(group, true);
			for( UserBean userbean : usersForGroup )
			{
				userIds.add(userbean.getUniqueID());
			}
			return userIds;
		}
		catch( Exception e )
		{
			throw new WorkflowException(e);
		}
	}

	protected boolean isOwner(String userid)
	{
		return getItem().getOwner().equals(userid);
	}

	protected void setOwner(String userid)
	{
		getItem().setOwner(userid);
	}

	protected String getUserId()
	{
		return CurrentUser.getUserID();
	}

	protected ItemKey getItemKey()
	{
		return params.getItemKey();
	}

	protected void setStopImmediately(boolean stop)
	{
		params.setStopImmediately(stop);
	}

	protected StagingFile getStaging()
	{
		ItemPack<Item> itemPack = getItemPack();
		if( itemPack != null )
		{
			String s = itemPack.getStagingID();
			if( !Check.isEmpty(s) )
			{
				return new StagingFile(s);
			}
		}
		return null;
	}

	protected void setVersion(int version)
	{
		getItem().setVersion(version);
	}

	protected void addAfterCommitEvent(ApplicationEvent<?> event)
	{
		params.getAfterCommitEvents().add(event);
	}

	public PropBagEx getItemXml()
	{
		return getItemPack().getXml();
	}

	public WorkflowParams getParams()
	{
		return params;
	}

	@Nullable
	@Override
	public ItemPack<Item> getItemPack()
	{
		return params.getItemPack();
	}

	@Override
	public boolean isReadOnly()
	{
		return true;
	}

	public Date getDateModified()
	{
		Item item = getItem();
		if( item == null || item.getDateModified() == null )
		{
			return params.getDateNow();
		}
		return item.getDateModified();
	}

	public ScriptContext createScriptContext(Map<String, Object> attributes)
	{
		return createScriptContext(attributes, null);
	}

	public ScriptContext createScriptContext(Map<String, Object> attributes, Map<String, Object> objects)
	{
		return workflowOpService.createScriptContext(getItemPack(), getStaging(), attributes, objects);
	}

	public void addNotifications(ItemKey itemKey, Collection<String> users, String reason, boolean batched)
	{
		params.setNotificationsAdded(true);
		params.setRequiresReindex(true);
		notificationService.addNotifications(itemKey, reason, users, batched);
	}

	public void removeNotificationsForItem(ItemId itemId, String... reason)
	{
		if( notificationService.removeAllForItem(itemId, Arrays.asList(reason)) > 0 )
		{
			params.setRequiresReindex(true);
		}
	}

	public void removeNotificationsForKey(ItemKey itemKey, String... reasons)
	{
		if( notificationService.removeAllForKey(itemKey, Arrays.asList(reasons)) > 0 )
		{
			params.setRequiresReindex(true);
		}
	}

	public void removeNotificationForUserAndKey(ItemKey itemKey, String userId, String... reasons)
	{
		if( notificationService.removeForUserAndKey(itemKey, userId, Arrays.asList(reasons)) > 0 )
		{
			params.setRequiresReindex(true);
		}
	}

	public ItemId getItemId()
	{
		return getItem().getItemId();
	}

	@PostConstruct
	protected void injected()
	{
		this.injected = true;
	}

	@Override
	public boolean failedToAutowire()
	{
		return !injected;
	}
}
