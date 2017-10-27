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

package com.tle.core.workflow.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.workflow.*;
import com.tle.common.workflow.node.ScriptNode;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.service.AbstractEntityService;

/**
 * @author Nicholas Read
 */
public interface WorkflowService extends AbstractEntityService<EntityEditingBean, Workflow>, RemoteWorkflowService
{
	/**
	 * Returns a list of moderators for a given workflow item. This is useful in
	 * the case of unanimous acceptance, where all users that must moderate this
	 * item. For this to succeed all the moderators must be defined as a
	 * specific user or a group. There is currently no means to determine all
	 * moderators if a role is specified, and <code>null</code> will be
	 * returned.
	 */
	Set<String> getAllModeratorUserIDs(PropBagEx itemxml, WorkflowItem workflow);

	boolean canCurrentUserModerate(PropBagEx itemxml, WorkflowItem task, WorkflowItemStatus status);

	String getLastRejectionMessage(Item item);

	boolean isOptionalTask(WorkflowItem workflowItem, PropBagEx itemxml);

	int getMessageCount(ItemKey itemKey);

	boolean cleanupMessageFiles(ItemKey itemKey);

	void cleanupMessageFiles(Collection<WorkflowNodeStatus> statuses);

	List<WorkflowMessage> getMessages(ItemKey itemKey);

	WorkflowItemStatus getIncompleteStatus(ItemTaskId itemTaskId);

	List<TaskModerator> getModeratorList(ItemTaskId taskId, boolean includeAccepted);

	List<WorkflowMessage> getCommentsForTask(ItemTaskId itemTaskId);

	Collection<BaseEntityLabel> listManagable();

	WorkflowItem getManageableTask(long taskId);

	int getItemCountForWorkflow(String uuid);

	Set<String> getUsersToNotifyOnScriptError(ScriptNode node);

	Set<String> getUsersToNotifyOnScriptCompletion(ScriptNode node);
}
