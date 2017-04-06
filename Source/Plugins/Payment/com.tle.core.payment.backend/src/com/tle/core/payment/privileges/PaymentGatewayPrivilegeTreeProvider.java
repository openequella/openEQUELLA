package com.tle.core.payment.privileges;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.payment.entity.PaymentGateway;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.payment.service.PaymentGatewayService;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class PaymentGatewayPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<PaymentGateway>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(PaymentGatewayPrivilegeTreeProvider.class);

	@Inject
	public PaymentGatewayPrivilegeTreeProvider(PaymentGatewayService gatewayService)
	{
		super(gatewayService, Node.ALL_PAYMENT_GATEWAYS, resources.key("securitytree.allgateways"),
			Node.PAYMENT_GATEWAY, resources.key("securitytree.targetallgateways"));
	}

	@Override
	protected PaymentGateway createEntity()
	{
		return new PaymentGateway();
	}
}
