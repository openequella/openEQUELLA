package com.tle.core.payment.storefront.operation;

import javax.inject.Inject;

import com.tle.beans.item.ItemId;
import com.tle.common.payment.storefront.entity.PurchasedContent;
import com.tle.core.payment.storefront.service.PurchaseService;
import com.tle.core.payment.storefront.service.PurchasedContentService;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

public class PurchasedItemUpdatedOperation extends AbstractWorkflowOperation
{
	public static final String REASON_PURCHASE_UPDATED = "piupdate"; //$NON-NLS-1$

	@Inject
	private PurchaseService purchaseService;
	@Inject
	private PurchasedContentService purchasedContentService;

	@Override
	public boolean execute()
	{
		PurchasedContent purchasedContent = purchasedContentService.getForLocalItem(getItemId());

		if( purchasedContent != null )
		{
			addNotifications(getItemId(), purchaseService.enumerateCheckoutByforItem(new ItemId(purchasedContent
				.getSourceItemUuid(), purchasedContent.getSourceItemVersion())), REASON_PURCHASE_UPDATED, true);
			return true;
		}
		else
		{
			return false;
		}
	}
}
