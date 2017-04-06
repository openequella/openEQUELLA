package com.tle.core.payment.privileges;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.payment.entity.TaxType;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.payment.service.TaxService;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

/**
 * @author Aaron
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class TaxPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<TaxType>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(TaxPrivilegeTreeProvider.class);

	@Inject
	public TaxPrivilegeTreeProvider(TaxService taxService)
	{
		super(taxService, Node.ALL_TAX, resources.key("securitytree.alltax"), Node.TAX, resources
			.key("securitytree.targetalltax"));
	}

	@Override
	protected TaxType createEntity()
	{
		return new TaxType();
	}
}
