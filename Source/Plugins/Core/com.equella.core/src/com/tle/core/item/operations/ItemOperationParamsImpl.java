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

package com.tle.core.item.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.workflow.SecurityStatus;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.item.NodeStatus;

/*
 * @author jmaginnis
 */
public final class ItemOperationParamsImpl implements ItemOperationParams
{
	private Date dateNow;
	private ItemKey itemKey;
	private ItemIdKey idKey;
	private ItemPack<Item> itemPack;
	private boolean stopImmediately;
	private boolean wentLive;
	private boolean update;
	private boolean modified;
	private boolean updateSecurity;
	private SecurityStatus securityStatus;
	private List<WorkflowOperation> extraOperations;
	private List<ItemOperationFilter> extraFilters;
	private Map<String, NodeStatus> statusMap;
	private WorkflowNodeStatus cause;
	private final List<AfterSaveHook> afterSave = new ArrayList<AfterSaveHook>();
	private final PriorityQueue<AfterCommitHook> afterCommit = new PriorityQueue<AfterCommitHook>(11,
		Ordering.from(new AfterCommitHookOrdering()).nullsLast());

	private final Map<String, String> attributes = new HashMap<String, String>();

	private final List<ApplicationEvent<?>> afterCommitEvents = new ArrayList<ApplicationEvent<?>>();
	private final List<ApplicationEvent<?>> afterCommitAllEvents = new ArrayList<ApplicationEvent<?>>();

	private String sessionID;
	private boolean notificationsAdded;
	private boolean requiresReindex;
	private Object securityObject;

	public ItemOperationParamsImpl()
	{
		this.dateNow = new Date();
	}

	@Override
	public void setItemPack(ItemPack itemPack)
	{
		this.itemPack = itemPack;
	}

	@Override
	public void reset(ItemKey itemKey, long key, ItemPack itemPack)
	{
		this.itemKey = itemKey;
		this.idKey = itemKey != null ? new ItemIdKey(key, itemKey) : null;
		this.itemPack = itemPack;
		afterSave.clear();
		afterCommitEvents.clear();
		afterCommit.clear();
		stopImmediately = false;
		wentLive = false;
		modified = false;
		updateSecurity = true;
		statusMap = null;
		notificationsAdded = false;
		requiresReindex = false;
		cause = null;
	}

	@Override
	public Date getDateNow()
	{
		return dateNow;
	}

	@Override
	public List<ApplicationEvent<?>> getAfterCommitEvents()
	{
		return afterCommitEvents;
	}

	@Override
	public List<ApplicationEvent<?>> getAfterCommitAllEvents()
	{
		return afterCommitAllEvents;
	}

	@Override
	public ItemKey getItemKey()
	{
		return itemKey;
	}

	@Override
	public void setItemKey(ItemKey itemId, long id)
	{
		this.itemKey = itemId;
		idKey = new ItemIdKey(id, itemId);
	}

	public String getSessionId()
	{
		return sessionID;
	}

	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

	public boolean isStopImmediately()
	{
		return stopImmediately;
	}

	@Override
	public void setStopImmediately(boolean stopImmediately)
	{
		this.stopImmediately = stopImmediately;
	}

	@Override
	public boolean isWentLive()
	{
		return wentLive;
	}

	@Override
	public void setWentLive(boolean wentLive)
	{
		this.wentLive = wentLive;
	}

	@Override
	public ItemPack<Item> getItemPack()
	{
		return itemPack;
	}

	@Override
	public boolean isUpdate()
	{
		return update;
	}

	@Override
	public void setUpdate(boolean update)
	{
		this.update = update;
	}

	@Override
	public boolean isModified()
	{
		return modified;
	}

	@Override
	public void setModified(boolean modified)
	{
		this.modified = modified;
	}

	@Override
	public boolean isUpdateSecurity()
	{
		return updateSecurity;
	}

	@Override
	public void setUpdateSecurity(boolean updateSecurity)
	{
		this.updateSecurity = updateSecurity;
	}

	@Override
	public void addOperation(WorkflowOperation operation)
	{
		if( extraOperations == null )
		{
			extraOperations = new ArrayList<WorkflowOperation>();
		}
		extraOperations.add(operation);
	}

	@Override
	public void addFilter(ItemOperationFilter filter)
	{
		if( extraFilters == null )
		{
			extraFilters = new ArrayList<ItemOperationFilter>();
		}
		extraFilters.add(filter);
	}

	@Override
	public List<WorkflowOperation> getOperations()
	{
		return extraOperations;
	}

	@Override
	public List<ItemOperationFilter> getExtraFilters()
	{
		return extraFilters;
	}

	@Override
	public SecurityStatus getSecurityStatus()
	{
		return securityStatus;
	}

	@Override
	public void setSecurityStatus(SecurityStatus securityStatus)
	{
		this.securityStatus = securityStatus;
	}

	@Override
	public Map<String, NodeStatus> getStatusMap()
	{
		return statusMap;
	}

	public void setDateNow(Date date)
	{
		dateNow = date;
	}

	@Override
	public void clearAllStatuses()
	{
		statusMap = new HashMap<String, NodeStatus>();
	}

	@Override
	public Map<String, String> getAttributes()
	{
		return Collections.unmodifiableMap(attributes);
	}

	@Override
	public void setAttribute(String name, String value)
	{
		attributes.put(name, value);
	}

	@Override
	public void addAfterCommitHook(int priority, Runnable runnable)
	{
		afterCommit.add(new AfterCommitHook(runnable, priority));
	}

	@Override
	public Collection<Runnable> getAfterCommit()
	{
		final Collection<Runnable> runnables = Collections2.transform(afterCommit,
			new Function<AfterCommitHook, Runnable>()
			{
				@Override
				public Runnable apply(AfterCommitHook o)
				{
					return o.runnable;
				}
			});
		return runnables;
	}

	private static final class AfterCommitHook
	{
		private final Runnable runnable;
		private final int priority;

		private AfterCommitHook(Runnable runnable, int priority)
		{
			this.runnable = runnable;
			this.priority = priority;
		}

		private int getPriority()
		{
			return priority;
		}
	}

	private static final class AfterCommitHookOrdering implements Comparator<AfterCommitHook>
	{
		@Override
		public int compare(AfterCommitHook o1, AfterCommitHook o2)
		{
			return o1.getPriority() - o2.getPriority();
		}
	}

	private static final class AfterSaveHook
	{
		private final Runnable runnable;

		private AfterSaveHook(Runnable runnable)
		{
			this.runnable = runnable;
		}
	}

	@Override
	public void addAfterSaveHook(Runnable runnable)
	{
		afterSave.add(new AfterSaveHook(runnable));
	}

	@Override
	public List<Runnable> getAfterSave()
	{
		return Lists.transform(afterSave, new Function<AfterSaveHook, Runnable>()
		{
			@Override
			public Runnable apply(AfterSaveHook as)
			{
				return as.runnable;
			}
		});
	}

	@Override
	public boolean isNotificationsAdded()
	{
		return notificationsAdded;
	}

	@Override
	public void setNotificationsAdded(boolean notificationsAdded)
	{
		this.notificationsAdded = notificationsAdded;
	}

	@Override
	public WorkflowNodeStatus getCause()
	{
		return cause;
	}

	@Override
	public void setCause(WorkflowNodeStatus cause)
	{
		this.cause = cause;
	}

	@Override
	public boolean isRequiresReindex()
	{
		return requiresReindex;
	}

	@Override
	public void setRequiresReindex(boolean requiresReindex)
	{
		this.requiresReindex = requiresReindex;
	}

	@Override
	public Object getSecurityObject()
	{
		if( securityObject == null )
		{
			return itemPack.getItem();
		}
		return securityObject;
	}

	@Override
	public void setSecurityObject(Object securityObject)
	{
		this.securityObject = securityObject;
	}

	@Override
	public ItemIdKey getItemIdKey()
	{
		return idKey;
	}
}
