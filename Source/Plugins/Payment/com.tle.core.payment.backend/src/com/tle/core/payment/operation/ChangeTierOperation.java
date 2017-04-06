package com.tle.core.payment.operation;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.ItemStatus;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.core.payment.PaymentSettings;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

@SecureItemStatus(value = {ItemStatus.PERSONAL}, not = true)
public class ChangeTierOperation extends AbstractWorkflowOperation
{
	private final ChangeTierState state;

	@Inject
	private PricingTierService tierService;
	@Inject
	private ConfigurationService configService;

	@AssistedInject
	public ChangeTierOperation(@Assisted ChangeTierState state)
	{
		this.state = state;
	}

	@Override
	public boolean execute()
	{
		PaymentSettings paymentSettings = configService.getProperties(new PaymentSettings());

		PricingTierAssignment assignmentTier = tierService.getPricingTierAssignmentForItem(getItemKey());
		if( assignmentTier != null )
		{
			if( paymentSettings.isFreeEnabled() )
			{
				assignmentTier.setFreeItem(state.isFree());
			}
			if( paymentSettings.isPurchaseEnabled() )
			{
				assignmentTier.setPurchasePricingTier(tierService.getByUuid(state.getPurchaseUuid()));
			}
			if( paymentSettings.isSubscriptionEnabled() )
			{
				assignmentTier.setSubscriptionPricingTier(tierService.getByUuid(state.getSubscriptionUuid()));
			}
			tierService.savePricingTierAssignment(assignmentTier);
		}
		else
		{
			tierService.createPricingTierAssignment(getItemKey(),
				paymentSettings.isPurchaseEnabled() ? tierService.getByUuid(state.getPurchaseUuid()) : null,
				paymentSettings.isSubscriptionEnabled() ? tierService.getByUuid(state.getSubscriptionUuid()) : null,
				paymentSettings.isFreeEnabled() ? state.isFree() : false);
		}

		return true;
	}
}
