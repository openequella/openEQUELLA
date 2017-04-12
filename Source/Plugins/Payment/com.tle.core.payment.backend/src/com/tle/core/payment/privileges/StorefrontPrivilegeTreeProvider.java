package com.tle.core.payment.privileges;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.payment.entity.StoreFront;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.payment.service.StoreFrontService;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class StorefrontPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<StoreFront>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(StorefrontPrivilegeTreeProvider.class);

	@Inject
	public StorefrontPrivilegeTreeProvider(StoreFrontService storefrontService)
	{
		super(storefrontService, Node.ALL_STOREFRONTS, resources.key("securitytree.allstorefronts"), Node.STOREFRONT,
			resources.key("securitytree.targetallstorefronts"));
	}

	@Override
	protected StoreFront createEntity()
	{
		return new StoreFront();
	}
}
