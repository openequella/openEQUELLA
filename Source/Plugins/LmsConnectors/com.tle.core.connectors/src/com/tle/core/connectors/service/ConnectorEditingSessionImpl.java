package com.tle.core.connectors.service;

import com.tle.common.EntityPack;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.services.entity.impl.EntityEditingSessionImpl;

/**
 * @author aholland
 */
public class ConnectorEditingSessionImpl extends EntityEditingSessionImpl<ConnectorEditingBean, Connector>
	implements
		ConnectorEditingSession
{
	private static final long serialVersionUID = 1L;

	public ConnectorEditingSessionImpl(String sessionId, EntityPack<Connector> pack, ConnectorEditingBean bean)
	{
		super(sessionId, pack, bean);
	}
}
