package com.tle.web.connectors.api;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.api.baseentity.serializer.AbstractEquellaBaseEntitySerializer;
import com.tle.web.connectors.api.bean.ConnectorBean;
import com.tle.web.connectors.api.impl.ConnectorEditorImpl.ConnectorEditorFactory;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
@Singleton
public class ConnectorBeanSerializer
	extends
		AbstractEquellaBaseEntitySerializer<Connector, ConnectorBean, ConnectorEditor>
{
	@Inject
	private ConnectorService connectorService;
	@Inject
	private ConnectorEditorFactory editorFactory;

	@Override
	protected ConnectorBean createBean()
	{
		return new ConnectorBean();
	}

	@Override
	protected Connector createEntity()
	{
		return new Connector();
	}

	@Override
	protected ConnectorEditor createExistingEditor(Connector entity, String stagingUuid, String lockId)
	{
		return editorFactory.createExistingEditor(entity, stagingUuid, lockId, true);
	}

	@Override
	protected ConnectorEditor createNewEditor(Connector entity, String stagingUuid)
	{
		return editorFactory.createNewEditor(entity, stagingUuid);
	}

	@Override
	protected void copyCustomFields(Connector connector, ConnectorBean bean, @Nullable Object data)
	{
		bean.setType(connector.getLmsType());
	}

	@Override
	protected AbstractEntityService<?, Connector> getEntityService()
	{
		return connectorService;
	}

	@Override
	protected Node getNonVirtualNode()
	{
		return Node.CONNECTOR;
	}
}
