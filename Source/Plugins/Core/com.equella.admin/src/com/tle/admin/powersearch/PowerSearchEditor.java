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
