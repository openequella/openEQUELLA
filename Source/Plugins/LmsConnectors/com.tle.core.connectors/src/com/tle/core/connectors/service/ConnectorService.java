package com.tle.core.connectors.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.connectors.ConnectorTypeDescriptor;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.connectors.service.RemoteConnectorService;
import com.tle.core.services.entity.AbstractEntityService;

/**
 * @author aholland
 */
public interface ConnectorService
	extends
		AbstractEntityService<ConnectorEditingBean, Connector>,
		RemoteConnectorService
{
	String ENTITY_TYPE = "CONNECTOR"; //$NON-NLS-1$

	Collection<ConnectorTypeDescriptor> listAllAvailableTypes();

	List<BaseEntityLabel> listExportable();

	List<BaseEntityLabel> listForViewing();

	Map<String, ConnectorTypeDescriptor> mapAllAvailableTypes();

	Connector getForEdit(String connectorUuid);

	boolean canViewContent(BaseEntityLabel connector);

	boolean canViewContent(Connector connector);

	boolean canExport(Connector connector);

	List<Connector> enumerateForUrl(String url);

}
