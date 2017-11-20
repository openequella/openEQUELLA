/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.connectors.service;

import javax.inject.Inject;

import com.dytech.edge.exceptions.WorkflowException;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.connectors.service.ConnectorItemKey;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.common.usermanagement.user.CurrentUser;
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
