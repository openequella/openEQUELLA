package com.tle.web.payment.fake.guice;

import com.google.inject.name.Names;
import com.tle.web.payment.fake.section.FakeCheckoutSection;
import com.tle.web.sections.equella.guice.SectionsModule;

/**
 * @author Aaron
 */
public class FakeGatewayModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("checkoutSection")).toProvider(checkoutTree());
	}

	private NodeProvider checkoutTree()
	{
		return node(FakeCheckoutSection.class);
	}
}
