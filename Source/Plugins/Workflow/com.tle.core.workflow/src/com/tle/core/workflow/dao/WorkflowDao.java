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

package com.tle.core.workflow.dao;

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
import com.tle.core.entity.dao.AbstractEntityDao;

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

	List<WorkflowItemStatus> findWorkflowItemStatusesForItem(Item item);

	WorkflowItem getTaskForItem(Item item, String taskId);

	int getCommentCount(ItemKey itemKey);

	List<WorkflowMessage> getMessages(ItemKey itemKey);

	WorkflowItemStatus getIncompleteStatus(ItemTaskId itemTaskId);

	List<WorkflowItem> getIncompleteTasks(Item item);

	WorkflowItem getWorkflowTaskById(long taskId);

	int getItemCountForWorkflow(String uuid);

	WorkflowNode getWorkflowNodeByUuid(String uuid);
}
