package com.tle.admin.dynacollection.tool;

import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.dynacollection.DynaCollectionEditor;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.entity.DynaCollection;
import com.tle.common.applet.client.ClientService;
import com.tle.common.dynacollection.RemoteDynaCollectionService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteAbstractEntityService;

public class DynaCollectionTool extends BaseEntityTool<DynaCollection>
{
	public DynaCollectionTool() throws Exception
	{
		super(DynaCollection.class, RemoteDynaCollectionService.ENTITY_TYPE);
	}

	@Override
	protected RemoteAbstractEntityService<DynaCollection> getService(ClientService client)
	{
		return client.getService(RemoteDynaCollectionService.class);
	}

	@Override
	protected String getErrorPath()
	{
		return "dynacollection"; //$NON-NLS-1$
	}

	@Override
	protected BaseEntityEditor<DynaCollection> createEditor(boolean readonly)
	{
		return new DynaCollectionEditor(this, readonly);
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.dynacollection.entityname"); //$NON-NLS-1$
	}
}
