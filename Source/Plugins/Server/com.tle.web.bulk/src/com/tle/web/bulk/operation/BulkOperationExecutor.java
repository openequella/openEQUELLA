package com.tle.web.bulk.operation;

import java.io.Serializable;

import com.tle.core.workflow.operations.WorkflowOperation;

public interface BulkOperationExecutor extends Serializable
{
	WorkflowOperation[] getOperations();

	String getTitleKey();
}
