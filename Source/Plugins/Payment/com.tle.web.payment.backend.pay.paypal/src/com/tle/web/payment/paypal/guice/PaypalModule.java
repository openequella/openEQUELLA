package com.tle.web.payment.paypal.guice;

import com.google.inject.name.Names;
import com.tle.web.payment.paypal.section.PaypalCheckoutSection;
import com.tle.web.sections.equella.guice.SectionsModule;

/**
 * @author Aaron
 */
public class PaypalModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("checkoutSection")).toProvider(checkoutTree());
	}

	private NodeProvider checkoutTree()
	{
		return node(PaypalCheckoutSection.class);
	}
}
