package com.tle.admin.payment.storefront;

import net.miginfocom.swing.MigLayout;

import com.tle.admin.collection.summarydisplay.AbstractOnlyTitleConfig;

public class PurchasedItemDisplayConfig extends AbstractOnlyTitleConfig
{
	@Override
	public void setup()
	{
		setLayout(new MigLayout());
		super.setup();
	}
}
