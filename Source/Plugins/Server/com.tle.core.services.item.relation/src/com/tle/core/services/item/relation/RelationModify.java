package com.tle.core.services.item.relation;

import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.services.item.relation.RelationOperation.RelationOperationFactory;
import com.tle.core.workflow.operations.DuringSaveOperation;
import com.tle.core.workflow.operations.WorkflowOperation;

/**
 * @author jmaginnis
 */
@SuppressWarnings("nls")
public class RelationModify extends FactoryMethodLocator<WorkflowOperation> implements DuringSaveOperation
{
	public static final String NAME = "editrelations";
	private static final long serialVersionUID = 1L;
	private final RelationOperationState state;

	public RelationModify(RelationOperationState state)
	{
		super(RelationOperationFactory.class, "create", state); //$NON-NLS-1$
		this.state = state;
	}

	public RelationOperationState getState()
	{
		return state;
	}

	@Override
	public WorkflowOperation createPostSaveWorkflowOperation()
	{
		return get();
	}

	@Override
	public WorkflowOperation createPreSaveWorkflowOperation()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return NAME;
	}
}
