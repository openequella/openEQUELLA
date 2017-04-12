package com.tle.core.connectors.service;

import javax.inject.Inject;

import com.dytech.edge.exceptions.WorkflowException;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.connectors.service.ConnectorItemKey;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;

public class MoveContentOperation extends AbstractContentOperation
{
	private final String courseId;
	private final String locationId;

	@Inject
	private ConnectorRepositoryService repositoryService;
	@Inject
	private ConnectorService connectorService;

	@AssistedInject
	public MoveContentOperation(@Assisted("courseId") String courseId, @Assisted("locationId") String locationId)
	{
		super();
		this.courseId = courseId;
		this.locationId = locationId;
	}

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
			boolean success = repositoryService.moveContent(connector, CurrentUser.getUsername(),
				itemKey.getContentId(), courseId, locationId);
			return success;
		}
		catch( LmsUserNotFoundException lms )
		{
			throw new WorkflowException(lms);
		}
	}
}
