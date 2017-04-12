package com.tle.core.payment.operation;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.core.guice.BindFactory;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

/**
 * See the notes for the associated class SaveNewPricingAssignmentOperation.
 * 
 * @author larry
 */
public class CommitAssignmentForNewItem extends AbstractWorkflowOperation
{
	@Inject
	private PricingTierService pricingTierService;
	private final PricingTierAssignment myAssignment;

	@Inject
	public CommitAssignmentForNewItem(@Assisted PricingTierAssignment pricingTierAssignment)
	{
		this.myAssignment = pricingTierAssignment;
	}

	@Override
	public boolean execute()
	{
		pricingTierService.savePricingTierAssignment(myAssignment);
		return false;
	}

	public void setPricingTierService(PricingTierService pricingTierService)
	{
		this.pricingTierService = pricingTierService;
	}

	@BindFactory
	public interface CommitAssignmentFactory
	{
		CommitAssignmentForNewItem create(PricingTierAssignment pricingTierAssignment);
	}
}
