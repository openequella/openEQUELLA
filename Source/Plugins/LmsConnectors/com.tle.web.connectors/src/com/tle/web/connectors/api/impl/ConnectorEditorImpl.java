package com.tle.web.connectors.api.impl;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.BindFactory;
import com.tle.core.services.entity.AbstractEntityService;
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
