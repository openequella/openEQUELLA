package com.tle.web.bulk.workflow.operations;

import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.ItemStatus;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;
import com.tle.core.item.standard.operations.workflow.TaskOperation;
import com.tle.core.security.impl.SecureInModeration;

@SecureInModeration
public class WorkflowRemoveOperation extends TaskOperation
{
	@Override
	public boolean execute()
	{
		createHistory(HistoryEvent.Type.workflowremoved);
		resetWithWorkflow(null);
		return true;
	}

}