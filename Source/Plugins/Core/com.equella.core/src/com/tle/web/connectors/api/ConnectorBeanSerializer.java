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

package com.tle.web.connectors.api;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
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
	protected ConnectorEditor createExistingEditor(Connector entity, String stagingUuid, String lockId,
		boolean importing)
	{
		return editorFactory.createExistingEditor(entity, stagingUuid, lockId, true, importing);
	}

	@Override
	protected ConnectorEditor createNewEditor(Connector entity, String stagingUuid, boolean importing)
	{
		return editorFactory.createNewEditor(entity, stagingUuid, importing);
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
