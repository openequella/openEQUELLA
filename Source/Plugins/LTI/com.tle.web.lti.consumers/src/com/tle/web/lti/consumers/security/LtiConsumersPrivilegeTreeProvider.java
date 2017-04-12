package com.tle.web.lti.consumers.security;

import javax.inject.Inject;

import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.lti.consumers.service.LtiConsumerService;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
public class LtiConsumersPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<LtiConsumer>
{
	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(LtiConsumersPrivilegeTreeProvider.class);

	@Inject
	protected LtiConsumersPrivilegeTreeProvider(LtiConsumerService consumerService)
	{
		super(consumerService, Node.ALL_LTI_CONSUMERS, RESOURCES.key("securitytree.alllticonsumers"),
			Node.LTI_CONSUMER, RESOURCES.key("securitytree.targeralllticonsumers"));
	}

	@Override
	protected LtiConsumer createEntity()
	{
		return new LtiConsumer();
	}

}
