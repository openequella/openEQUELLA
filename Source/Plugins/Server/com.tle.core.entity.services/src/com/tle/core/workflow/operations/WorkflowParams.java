/*
 * Created on Oct 7, 2004 For "The Learning Edge"
 */
package com.tle.core.workflow.operations;

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
import com.tle.common.WorkflowOperationParams;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.workflow.filters.WorkflowFilter;
import com.tle.core.workflow.nodes.NodeStatus;

/*
 * @author jmaginnis
 */
public final class WorkflowParams implements WorkflowOperationParams
{
	public static final int COMMIT_HOOK_PRIORITY_LOW = 1000;
	public static final int COMMIT_HOOK_PRIORITY_MEDIUM = 500;
	public static final int COMMIT_HOOK_PRIORITY_HIGH = 0;

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
	private List<WorkflowFilter> extraFilters;
	private Map<String, NodeStatus> statusMap;
	private WorkflowNodeStatus cause;
	private final List<AfterSaveHook> afterSave = new ArrayList<AfterSaveHook>();
	private final PriorityQueue<AfterCommitHook> afterCommit = new PriorityQueue<AfterCommitHook>(11, Ordering.from(
		new AfterCommitHookOrdering()).nullsLast());

	private final Map<String, String> attributes = new HashMap<String, String>();

	private final List<ApplicationEvent<?>> afterCommitEvents = new ArrayList<ApplicationEvent<?>>();
	private final List<ApplicationEvent<?>> afterCommitAllEvents = new ArrayList<ApplicationEvent<?>>();

	private String sessionID;
	private boolean notificationsAdded;
	private boolean requiresReindex;
	private Object securityObject;

	public void setItemPack(ItemPack itemPack)
	{
		this.itemPack = itemPack;
	}

	public WorkflowParams()
	{
		this.dateNow = new Date();
	}

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

	public Date getDateNow()
	{
		return dateNow;
	}

	public List<ApplicationEvent<?>> getAfterCommitEvents()
	{
		return afterCommitEvents;
	}

	public List<ApplicationEvent<?>> getAfterCommitAllEvents()
	{
		return afterCommitAllEvents;
	}

	public ItemKey getItemKey()
	{
		return itemKey;
	}

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

	public void setStopImmediately(boolean stopImmediately)
	{
		this.stopImmediately = stopImmediately;
	}

	public boolean isWentLive()
	{
		return wentLive;
	}

	public void setWentLive(boolean wentLive)
	{
		this.wentLive = wentLive;
	}

	public ItemPack<Item> getItemPack()
	{
		return itemPack;
	}

	public boolean isUpdate()
	{
		return update;
	}

	public void setUpdate(boolean update)
	{
		this.update = update;
	}

	public boolean isModified()
	{
		return modified;
	}

	public void setModified(boolean modified)
	{
		this.modified = modified;
	}

	public boolean isUpdateSecurity()
	{
		return updateSecurity;
	}

	public void setUpdateSecurity(boolean updateSecurity)
	{
		this.updateSecurity = updateSecurity;
	}

	public void addOperation(WorkflowOperation operation)
	{
		if( extraOperations == null )
		{
			extraOperations = new ArrayList<WorkflowOperation>();
		}
		extraOperations.add(operation);
	}

	public void addFilter(WorkflowFilter filter)
	{
		if( extraFilters == null )
		{
			extraFilters = new ArrayList<WorkflowFilter>();
		}
		extraFilters.add(filter);
	}

	public List<WorkflowOperation> getOperations()
	{
		return extraOperations;
	}

	public List<WorkflowFilter> getExtraFilters()
	{
		return extraFilters;
	}

	public SecurityStatus getSecurityStatus()
	{
		return securityStatus;
	}

	public void setSecurityStatus(SecurityStatus securityStatus)
	{
		this.securityStatus = securityStatus;
	}

	public Map<String, NodeStatus> getStatusMap()
	{
		return statusMap;
	}

	public void setDateNow(Date date)
	{
		dateNow = date;
	}

	public void clearAllStatuses()
	{
		statusMap = new HashMap<String, NodeStatus>();
	}

	public Map<String, String> getAttributes()
	{
		return Collections.unmodifiableMap(attributes);
	}

	@Override
	public void setAttribute(String name, String value)
	{
		attributes.put(name, value);
	}

	public void addAfterCommitHook(int priority, Runnable runnable)
	{
		afterCommit.add(new AfterCommitHook(runnable, priority));
	}

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

	public void addAfterSaveHook(Runnable runnable)
	{
		afterSave.add(new AfterSaveHook(runnable));
	}

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

	public boolean isNotificationsAdded()
	{
		return notificationsAdded;
	}

	public void setNotificationsAdded(boolean notificationsAdded)
	{
		this.notificationsAdded = notificationsAdded;
	}

	public WorkflowNodeStatus getCause()
	{
		return cause;
	}

	public void setCause(WorkflowNodeStatus cause)
	{
		this.cause = cause;
	}

	public boolean isRequiresReindex()
	{
		return requiresReindex;
	}

	public void setRequiresReindex(boolean requiresReindex)
	{
		this.requiresReindex = requiresReindex;
	}

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

	public ItemIdKey getItemIdKey()
	{
		return idKey;
	}
}
