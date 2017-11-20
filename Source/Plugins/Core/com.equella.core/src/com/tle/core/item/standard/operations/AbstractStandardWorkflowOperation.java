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

package com.tle.core.item.standard.operations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.exceptions.WorkflowException;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.workflow.Workflow;
import com.tle.core.item.operations.AbstractWorkflowOperation;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.notification.NotificationService;
import com.tle.core.notification.beans.Notification;
import com.tle.core.services.user.UserService;

/**
 * @author jmaginnis
 */
public abstract class AbstractStandardWorkflowOperation extends AbstractWorkflowOperation
{
	protected static final Logger LOGGER = Logger.getLogger(AbstractStandardWorkflowOperation.class);

	@Inject
	protected UserService userService;
	@Inject
	protected ItemOperationFactory operationFactory;
	@Inject
	protected NotificationService notificationService;

	public Workflow getWorkflow()
	{
		return getCollection().getWorkflow();
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
					removeNotificationsForItem(getItemId(), Notification.REASON_WENTLIVE,
						Notification.REASON_WENTLIVE2);
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
		ItemDefinition myItemDef = getCollection();
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

	public ScriptContext createScriptContext(Map<String, Object> attributes)
	{
		return createScriptContext(attributes, null);
	}

	public ScriptContext createScriptContext(Map<String, Object> attributes, Map<String, Object> objects)
	{
		return itemService.createScriptContext(getItemPack(), getStaging(), attributes, objects);
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
}
