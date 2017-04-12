package com.tle.core.connectors.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.connectors.entity.Connector;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ConnectorPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<Connector>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(ConnectorPrivilegeTreeProvider.class);

	@Inject
	public ConnectorPrivilegeTreeProvider(ConnectorService connectorService)
	{
		super(connectorService, Node.ALL_CONNECTORS, resources.key("securitytree.allconnectors"), Node.CONNECTOR,
			resources.key("securitytree.targetallconnectors"));
	}

	@Override
	protected Connector createEntity()
	{
		return new Connector();
	}
}
