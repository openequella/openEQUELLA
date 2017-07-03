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

package com.tle.web.connectors.api.impl;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.BindFactory;
import com.tle.web.api.baseentity.serializer.AbstractBaseEntityEditor;
import com.tle.web.connectors.api.ConnectorEditor;
import com.tle.web.connectors.api.bean.ConnectorBean;

/**
 * @author Aaron
 */
@NonNullByDefault
public class ConnectorEditorImpl extends AbstractBaseEntityEditor<Connector, ConnectorBean> implements ConnectorEditor
{
	@Inject
	private ConnectorService connectorService;

	@AssistedInject
	public ConnectorEditorImpl(@Assisted Connector entity, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("lockId") @Nullable String lockId, @Assisted("editing") boolean editing,
		@Assisted("importing") boolean importing)
	{
		super(entity, stagingUuid, lockId, editing, importing);
	}

	@AssistedInject
	public ConnectorEditorImpl(@Assisted Connector entity, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("importing") boolean importing)
	{
		this(entity, stagingUuid, null, false, importing);
	}

	@Override
	protected void copyCustomFields(ConnectorBean bean)
	{
		super.copyCustomFields(bean);
		entity.setLmsType(bean.getType());
	}

	@Override
	protected AbstractEntityService<?, Connector> getEntityService()
	{
		return connectorService;
	}

	@BindFactory
	public interface ConnectorEditorFactory
	{
		ConnectorEditorImpl createExistingEditor(@Assisted Connector connector,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
			@Assisted("editing") boolean editing, @Assisted("importing") boolean importing);

		ConnectorEditorImpl createNewEditor(@Assisted Connector connector,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("importing") boolean importing);
	}
}
