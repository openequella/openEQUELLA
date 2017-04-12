package com.tle.core.payment.operation;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;

@BindFactory
public interface OperationFactory
{
	ChangeTierOperation createChangeTier(@Assisted ChangeTierState state);

	ChangeCatalogueOperation createChangeCatalogue(@Assisted ChangeCatalogueState state);

	ItemSaleOperation itemSold();
}
