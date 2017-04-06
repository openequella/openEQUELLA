package com.tle.core.payment.storefront.operation;

import com.tle.core.guice.BindFactory;

@BindFactory
public interface OperationFactory
{
	PurchasedItemUpdatedOperation itemUpdated();
}
