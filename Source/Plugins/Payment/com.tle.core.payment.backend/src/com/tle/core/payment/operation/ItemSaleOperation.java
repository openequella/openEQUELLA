package com.tle.core.payment.operation;

import com.tle.core.payment.PaymentConstants;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

public class ItemSaleOperation extends AbstractWorkflowOperation
{
	@Override
	public boolean execute()
	{
		addNotifications(getItemId(), getAllOwnerIds(), PaymentConstants.NOTIFICATION_REASON_ITEMSALE, true);
		return true;
	}

}
