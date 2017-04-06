package com.tle.web.bulk.operation;

import java.io.Serializable;

import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.core.workflow.operations.WorkflowOperation;

public class SimpleOperationExecutor extends FactoryMethodLocator<WorkflowOperation> implements BulkOperationExecutor
{
	private static final long serialVersionUID = 1L;

	private final Class<? extends WorkflowOperation> operationClass;
	private final boolean save;

	public SimpleOperationExecutor(Class<? extends WorkflowOperation> operationClass, String methodName, boolean save,
		Serializable... args)
	{
		super(WorkflowFactory.class, methodName, args);
		this.operationClass = operationClass;
		this.save = save;
	}

	@Override
	public WorkflowOperation[] getOperations()
	{
		WorkflowFactory factory = getFactory();
		WorkflowOperation op = invokeFactoryMethod(factory);
		operationClass.cast(op);
		if( !save )
		{
			return new WorkflowOperation[]{op};
		}
		return new WorkflowOperation[]{op, factory.saveBackground()};
	}

	@Override
	public String getTitleKey()
	{
		return operationClass.getName();
	}
}
