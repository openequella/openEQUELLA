package com.tle.core.connectors.service;

import javax.inject.Inject;

import com.dytech.edge.exceptions.WorkflowException;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.connectors.service.ConnectorItemKey;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;

public class RemoveContentOperation extends AbstractContentOperation
{
	@Inject
	private ConnectorRepositoryService repositoryService;
	@Inject
	private ConnectorService connectorService;

	@Override
	public boolean execute()
	{
		ConnectorItemKey itemKey = (ConnectorItemKey) params.getItemKey();
		Connector connector = connectorService.get(itemKey.getConnectorId());
		if( !connectorService.canExport(connector) )
		{
			throw new AccessDeniedException(CurrentLocale.get("com.tle.core.connectors.bulk.error.permission")); //$NON-NLS-1$
		}

		try
		{
			boolean success = repositoryService.deleteContent(connector, CurrentUser.getUsername(),
				itemKey.getContentId());
			return success;
		}
		catch( LmsUserNotFoundException lms )
		{
			throw new WorkflowException(lms);
		}
	}
}
