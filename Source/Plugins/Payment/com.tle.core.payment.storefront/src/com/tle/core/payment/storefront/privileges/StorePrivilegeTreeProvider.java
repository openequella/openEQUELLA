package com.tle.core.payment.storefront.privileges;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.payment.storefront.entity.Store;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.payment.storefront.service.StoreService;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
public class StorePrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<Store>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(StorePrivilegeTreeProvider.class);

	@Inject
	public StorePrivilegeTreeProvider(StoreService storeService)
	{
		super(storeService, Node.ALL_STORES, resources.key("securitytree.allstores"), Node.STORE, resources
			.key("securitytree.targetallstores"));
	}

	@Override
	protected Store createEntity()
	{
		return new Store();
	}

}
