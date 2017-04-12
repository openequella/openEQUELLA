package com.tle.core.connectors.service;

import com.tle.common.connectors.entity.Connector;

public interface ConnectorServiceExtension
{
	void deleteExtra(Connector connector);

	void edit(Connector entity, ConnectorEditingBean bean);

	void add(Connector connector);

	void loadExtra(Connector connector);
}
