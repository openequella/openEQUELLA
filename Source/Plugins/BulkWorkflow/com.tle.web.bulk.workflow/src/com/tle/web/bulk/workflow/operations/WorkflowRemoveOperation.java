package com.tle.web.bulk.workflow.operations;

import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.ItemStatus;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;
import com.tle.core.security.impl.SecureInModeration;

@SecureInModeration
public class WorkflowRemoveOperation extends AbstractStandardWorkflowOperation
{
	@Override
	public boolean execute()
	{
		createHistory(HistoryEvent.Type.workflowremoved);
		setState(ItemStatus.LIVE);
		getItem().setModerating(false);
		return true;
	}

}