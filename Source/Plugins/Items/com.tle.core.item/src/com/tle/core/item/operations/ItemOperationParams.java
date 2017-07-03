package com.tle.core.item.operations;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.workflow.SecurityStatus;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.item.NodeStatus;

/**
 * @author aholland
 */
public interface ItemOperationParams
{
	public static final int COMMIT_HOOK_PRIORITY_LOW = 1000;
	public static final int COMMIT_HOOK_PRIORITY_MEDIUM = 500;
	public static final int COMMIT_HOOK_PRIORITY_HIGH = 0;

	void setAttribute(String name, String value);

	Date getDateNow();

	ItemKey getItemKey();

	void setItemKey(ItemKey itemKey, long id);

	ItemIdKey getItemIdKey();

	void setStopImmediately(boolean stopImmediately);

	ItemPack<Item> getItemPack();

	List<ApplicationEvent<?>> getAfterCommitEvents();

	List<ApplicationEvent<?>> getAfterCommitAllEvents();

	void setUpdate(boolean update);

	boolean isUpdate();

	void setItemPack(ItemPack<Item> itemPack);

	Map<String, String> getAttributes();

	void setModified(boolean modified);

	boolean isModified();

	void setUpdateSecurity(boolean update);

	boolean isUpdateSecurity();

	void setWentLive(boolean wentLive);

	boolean isWentLive();

	void setRequiresReindex(boolean requiresReindex);

	boolean isRequiresReindex();

	void setNotificationsAdded(boolean notificationsAdded);

	boolean isNotificationsAdded();

	void addAfterSaveHook(Runnable runnable);

	Collection<Runnable> getAfterCommit();

	List<Runnable> getAfterSave();

	void addAfterCommitHook(int priority, Runnable runnable);

	void setSecurityObject(Object object);

	Object getSecurityObject();

	SecurityStatus getSecurityStatus();

	void setSecurityStatus(SecurityStatus securityStatus);

	void addFilter(ItemOperationFilter filter);

	List<ItemOperationFilter> getExtraFilters();

	void addOperation(WorkflowOperation operation);

	void setCause(WorkflowNodeStatus status);

	WorkflowNodeStatus getCause();

	List<WorkflowOperation> getOperations();

	void reset(ItemKey itemKey, long key, ItemPack itemPack);

	Map<String, NodeStatus> getStatusMap();

	void clearAllStatuses();
}
