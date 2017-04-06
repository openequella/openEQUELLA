package com.tle.core.payment.privileges;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.payment.entity.PricingTier;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class PricingTierPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<PricingTier>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(PricingTierPrivilegeTreeProvider.class);

	@Inject
	public PricingTierPrivilegeTreeProvider(PricingTierService regionService)
	{
		super(regionService, Node.ALL_TIERS, resources.key("securitytree.alltiers"), Node.TIER, resources
			.key("securitytree.targetalltiers"));
	}

	@Override
	protected PricingTier createEntity()
	{
		return new PricingTier();
	}
}
