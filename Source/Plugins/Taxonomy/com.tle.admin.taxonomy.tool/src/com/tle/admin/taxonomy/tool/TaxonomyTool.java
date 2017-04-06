package com.tle.admin.taxonomy.tool;

import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.taxonomy.RemoteTaxonomyService;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.core.remoting.RemoteAbstractEntityService;

public class TaxonomyTool extends BaseEntityTool<Taxonomy>
{
	public TaxonomyTool()
	{
		super(Taxonomy.class, RemoteTaxonomyService.ENTITY_TYPE);
	}

	@Override
	protected RemoteAbstractEntityService<Taxonomy> getService(ClientService client)
	{
		return client.getService(RemoteTaxonomyService.class);
	}

	@Override
	protected String getErrorPath()
	{
		return "taxonomy"; //$NON-NLS-1$
	}

	@Override
	protected BaseEntityEditor<Taxonomy> createEditor(boolean readonly)
	{
		return new TaxonomyEditor(this, readonly);
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.taxonomy.tool.entityname"); //$NON-NLS-1$
	}
}
