package com.tle.admin.schema.tool;

import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.schema.manager.SchemaManager;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.entity.Schema;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteAbstractEntityService;
import com.tle.core.remoting.RemoteSchemaService;

public class SchemaTool extends BaseEntityTool<Schema>
{
	public SchemaTool() throws Exception
	{
		super(Schema.class, RemoteSchemaService.ENTITY_TYPE);
	}

	@Override
	protected RemoteAbstractEntityService<Schema> getService(ClientService client)
	{
		return client.getService(RemoteSchemaService.class);
	}

	@Override
	protected BaseEntityEditor<Schema> createEditor(boolean readonly)
	{
		return new SchemaManager(this, readonly);
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.gui.schematool.name");
	}

	@Override
	protected String getErrorPath()
	{
		return "schema";
	}
}
