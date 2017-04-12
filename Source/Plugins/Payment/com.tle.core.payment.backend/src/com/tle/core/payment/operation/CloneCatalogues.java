package com.tle.core.payment.operation;

import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.workflow.operations.DuringSaveOperation;
import com.tle.core.workflow.operations.WorkflowOperation;

/**
 * @author Aaron
 */
public class CloneCatalogues extends FactoryMethodLocator<WorkflowOperation> implements DuringSaveOperation
{
	public CloneCatalogues(ChangeCatalogueState state)
	{
		super(OperationFactory.class, "createChangeCatalogue", state);
	}

	@Override
	public WorkflowOperation createPreSaveWorkflowOperation()
	{
		return null;
	}

	@Override
	public WorkflowOperation createPostSaveWorkflowOperation()
	{
		return get();
	}

	@Override
	public String getName()
	{
		return null;
	}
}
