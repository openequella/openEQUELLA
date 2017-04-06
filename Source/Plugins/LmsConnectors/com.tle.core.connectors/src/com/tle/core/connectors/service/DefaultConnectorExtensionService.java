package com.tle.core.connectors.service;

import javax.inject.Singleton;

import com.tle.common.connectors.entity.Connector;
import com.tle.core.guice.Bind;

@Bind
@Singleton
public class DefaultConnectorExtensionService implements ConnectorServiceExtension
{
	@Override
	public void deleteExtra(Connector connector)
	{
		// Nothing by default
	}

	@Override
	public void edit(Connector entity, ConnectorEditingBean bean)
	{
		// Nothing by default
	}

	@Override
	public void add(Connector connector)
	{
		// Nothing by default
	}

	@Override
	public void loadExtra(Connector connector)
	{
		// Nothing by default
	}
}
