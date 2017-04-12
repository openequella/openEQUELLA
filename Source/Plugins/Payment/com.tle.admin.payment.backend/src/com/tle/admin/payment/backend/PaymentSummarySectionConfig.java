package com.tle.admin.payment.backend;

import net.miginfocom.swing.MigLayout;

import com.tle.admin.collection.summarydisplay.AbstractOnlyTitleConfig;

public class PaymentSummarySectionConfig extends AbstractOnlyTitleConfig
{
	@Override
	public void setup()
	{
		setLayout(new MigLayout());
		super.setup();
	}
}
