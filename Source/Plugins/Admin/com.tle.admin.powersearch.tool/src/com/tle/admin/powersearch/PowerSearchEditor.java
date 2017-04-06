/*
 * Created on May 10, 2005
 */
package com.tle.admin.powersearch;

import java.util.ArrayList;
import java.util.List;

import com.tle.admin.baseentity.AccessControlTab;
import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.entity.PowerSearch;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;

/**
 * @author Nicholas Read
 */
public class PowerSearchEditor extends BaseEntityEditor<PowerSearch>
{
	private SchemaModel schema;

	public PowerSearchEditor(BaseEntityTool<PowerSearch> tool, boolean readonly)
	{
		super(tool, readonly);
		schema = new SchemaModel();
	}

	@Override
	protected AbstractDetailsTab<PowerSearch> constructDetailsTab()
	{
		return new DetailsTab();
	}

	@Override
	protected List<BaseEntityTab<PowerSearch>> getTabs()
	{
		List<BaseEntityTab<PowerSearch>> tabs1 = new ArrayList<BaseEntityTab<PowerSearch>>();

		tabs1.add((DetailsTab) detailsTab);
		tabs1.add(new PowerSearchTab());
		tabs1.add(new AccessControlTab<PowerSearch>(Node.POWER_SEARCH));
		return tabs1;
	}

	@Override
	public void addTab(BaseEntityTab<PowerSearch> tab)
	{
		if( tab instanceof AbstractPowerSearchTab )
		{
			AbstractPowerSearchTab atab = (AbstractPowerSearchTab) tab;
			atab.setSchemaModel(schema);
			atab.setParentChangeDetector(this);
		}

		super.addTab(tab);
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.powersearch.powersearcheditor.entityname"); //$NON-NLS-1$
	}

	@Override
	protected String getWindowTitle()
	{
		return CurrentLocale.get("com.tle.admin.powersearch.powersearcheditor.title"); //$NON-NLS-1$
	}

	@Override
	public String getDocumentName()
	{
		return CurrentLocale.get("com.tle.admin.powersearch.powersearcheditor.name"); //$NON-NLS-1$
	}
}
