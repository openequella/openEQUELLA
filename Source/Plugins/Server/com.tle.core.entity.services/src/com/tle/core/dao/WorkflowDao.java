/*
 * Created on Oct 26, 2005
 */
package com.tle.core.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemTaskId;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;

/**
 * @author Nicholas Read
 */
public interface WorkflowDao extends AbstractEntityDao<Workflow>
{
	void markForReset(Set<WorkflowNode> delNodes);

	Collection<WorkflowItem> findTasksForGroup(String groupID);

	Collection<WorkflowItem> findTasksForUser(String userID);

	// For user re-IDing
	Collection<WorkflowMessage> findMessagesForUser(String userID);

	// For user re-IDing
	Collection<WorkflowItemStatus> findWorkflowItemStatusesForUser(String userID);

	// For user re-IDing
	Collection<ModerationStatus> findModerationStatusesForUser(String userID);

	WorkflowItem getTaskForItem(Item item, String taskId);

	int getCommentCount(ItemKey itemKey);

	List<WorkflowMessage> getMessages(ItemKey itemKey);

	WorkflowItemStatus getIncompleteStatus(ItemTaskId itemTaskId);

	List<WorkflowItem> getIncompleteTasks(Item item);

	WorkflowItem getWorkflowTaskById(long taskId);

	int getItemCountForWorkflow(String uuid);
}
