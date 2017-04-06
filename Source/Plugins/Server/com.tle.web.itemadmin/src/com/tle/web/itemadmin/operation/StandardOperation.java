/**
 *
 */
package com.tle.web.itemadmin.operation;

import java.io.Serializable;

import com.tle.core.workflow.operations.WorkflowOperation;
import com.tle.web.bulk.operation.SimpleOperationExecutor;

public final class StandardOperation
{
	private final Class<? extends WorkflowOperation> opClass;
	private final String operationId;
	private final boolean save;
	private final String methodName;
	private final Serializable[] args;

	public StandardOperation(String operationId, Class<? extends WorkflowOperation> opClass, String methodName)
	{
		this(operationId, opClass, methodName, true);
	}

	public StandardOperation(String operationId, Class<? extends WorkflowOperation> opClass, String methodName,
		boolean save, Serializable... args)
	{
		this.operationId = operationId;
		this.opClass = opClass;
		this.save = save;
		this.methodName = methodName;
		this.args = args;
	}

	public SimpleOperationExecutor getExecutor()
	{
		return new SimpleOperationExecutor(opClass, methodName, save, args);
	}

	public String getOperationId()
	{
		return operationId;
	}
}