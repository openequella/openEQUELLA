package com.tle.web.bulk.workflowtask.operations;

import com.google.inject.Inject;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.item.standard.operations.workflow.TaskOperation;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.bulk.operation.BulkOperationService;

/**
 * @author Aaron
 *
 */
public abstract class AbstractBulkTaskOperation extends TaskOperation
{
	@Inject
	private TLEAclManager aclService;

	protected ItemTaskId getTaskId()
	{
		return new ItemTaskId(getItemKey().toString());
	}

	protected TaskStatus init()
	{
		ItemTaskId taskId = getTaskId();
		TaskStatus status = (TaskStatus) getNodeStatus(taskId.getTaskId());
		Item item = getItem();

		WorkflowNode workflowNode = status.getWorkflowNode();
		if( !aclService.checkPrivilege("MANAGE_WORKFLOW", workflowNode.getWorkflow()) )
		{
			throw new AccessDeniedException(CurrentLocale.get("com.tle.core.services.item.error.nopriv.moderation",
				"MANAGE_WORKFLOW", CurrentLocale.get(item.getName()), CurrentLocale.get(workflowNode.getName())));
		}

		//setup the result name for result reporting
		String stepName = CurrentLocale.get(workflowNode.getName(), workflowNode.getUuid());
		getItemPack().setAttribute(BulkOperationService.KEY_ITEM_RESULT_TITLE,
			CurrentLocale.get(item.getName(), item.getUuid()) + " - " + stepName.toString());

		return status;
	}
}
