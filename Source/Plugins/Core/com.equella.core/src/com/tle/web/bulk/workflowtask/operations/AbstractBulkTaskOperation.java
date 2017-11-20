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

package com.tle.web.bulk.workflowtask.operations;

import com.dytech.edge.exceptions.WorkflowException;
import com.google.inject.Inject;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.item.standard.operations.workflow.TaskOperation;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.exceptions.PrivilegeRequiredException;
import com.tle.web.bulk.operation.BulkOperationService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

/**
 * @author Aaron
 *
 */
public abstract class AbstractBulkTaskOperation extends TaskOperation
{
	@Inject
	private TLEAclManager aclService;
	private static PluginResourceHelper helper = ResourcesService.getResourceHelper(TaskRejectOperation.class);

	protected ItemTaskId getTaskId()
	{
		return new ItemTaskId(getItemKey().toString());
	}

	protected TaskStatus init(String... privs)
	{
		ItemTaskId taskId = getTaskId();
		Item item = getItem();

		WorkflowNode workflowNode = getWorkflow().getAllNodesAsMap().get(taskId.getTaskId());
		if( aclService.filterNonGrantedPrivileges(workflowNode.getWorkflow(), privs).isEmpty() )
		{
			throw new PrivilegeRequiredException(privs);
		}

		//setup the result name for result reporting
		String stepName = CurrentLocale.get(workflowNode.getName(), workflowNode.getUuid());
		getItemPack().setAttribute(BulkOperationService.KEY_ITEM_RESULT_TITLE,
			CurrentLocale.get(item.getName(), item.getUuid()) + " - " + stepName.toString());

		TaskStatus taskStatus = (TaskStatus) getNodeStatus(taskId.getTaskId());
		if (taskStatus == null)
		{
			throw new WorkflowException(CurrentLocale.get(helper.key("bulkop.task.nolonger")));
		}
		return taskStatus;
	}
}
