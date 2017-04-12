package com.tle.core.payment.scripting;

import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.core.payment.operation.CommitAssignmentForNewItem.CommitAssignmentFactory;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.workflow.operations.DuringSaveOperation;
import com.tle.core.workflow.operations.WorkflowOperation;

/**
 * For the purposes of enabling scripting of PricingTierAssignments while
 * creating a new item in a collection wizard, it is necessary to first save the
 * (new) item before we can save the PricingTierAssignment for that item
 * (otherwise a transient instance exception occurs on save). Accordingly this
 * class is added as a postSave workflow operation to the current wizard state
 * by the PricingTierScriptObject. On wizard.save, the associated class
 * CommitAssignmentForNewItem is invoked to perform the save of the
 * PricingTierAssigment
 * 
 * @author larry
 */
public class SaveNewPricingAssignmentOperation extends FactoryMethodLocator<WorkflowOperation>
	implements
		DuringSaveOperation
{
	private static final long serialVersionUID = 1L;

	public SaveNewPricingAssignmentOperation(PricingTierAssignment pricingTierAssignment)
	{
		super(CommitAssignmentFactory.class, "create", pricingTierAssignment); //$NON-NLS-1$
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
		return null;
	}
}
