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

package com.tle.admin.itemdefinition;

import java.util.ArrayList;
import java.util.List;

import com.dytech.edge.admin.script.options.ItemdefScriptOptions;
import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.WizardTree;
import com.tle.admin.baseentity.AccessControlTab;
import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.collection.searchdisplay.SearchDetailsTemplateTab;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree;

/**
 * An editor and creation tool for item definitions. This is generally used and
 * lauched in coordination with the Learning Edge administration tool.
 * 
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class ItemEditor extends BaseEntityEditor<ItemDefinition>
{
	private SchemaModel schema;
	private ItemdefScriptOptions scriptOptions;

	public ItemEditor(BaseEntityTool<ItemDefinition> tool, boolean readonly)
	{
		super(tool, readonly);
	}

	@Override
	protected AbstractDetailsTab<ItemDefinition> constructDetailsTab()
	{
		return new DetailsTab();
	}

	@Override
	protected List<? extends BaseEntityTab<ItemDefinition>> getTabs()
	{
		WizardTree tree = new WizardTree(WizardHelper.WIZARD_TYPE_CONTRIBUTION, driver.getControlRepository());

		ArrayList<BaseEntityTab<ItemDefinition>> itemdefTabs = new ArrayList<BaseEntityTab<ItemDefinition>>();
		itemdefTabs.add((DetailsTab) detailsTab);
		itemdefTabs.add(new WizardTab(tree, this));

		ItemdefExtraTabs security = new ItemdefExtraTabs(
			CurrentLocale.get("com.tle.admin.itemdefinition.itemeditor.security"));
		security.add(new AccessControlTab<ItemDefinition>(PrivilegeTree.Node.COLLECTION));
		security.add(new ItemStatusAccessControlTab());
		security.add(new ItemMetadataAccessControlTab(tree, this));
		security.add(new DynamicMetadataAccessControlTab());
		itemdefTabs.add(security);

		ItemdefExtraTabs display = new ItemdefExtraTabs(
			CurrentLocale.get("com.tle.admin.itemdefinition.itemeditor.display"));
		display.add(new ItemSummaryTemplateTab());
		display.add(new SearchDetailsTemplateTab());
		itemdefTabs.add(display);

		itemdefTabs.add(new MapperTab());
		itemdefTabs.add(new ExpertScriptingTab(this));
		itemdefTabs.add(new ExtensionsTab());

		return itemdefTabs;
	}

	@Override
	public void addTab(BaseEntityTab<ItemDefinition> tab)
	{
		if( tab instanceof AbstractItemdefTab )
		{
			AbstractItemdefTab atab = (AbstractItemdefTab) tab;
			atab.setSchemaModel(schema);
			atab.setOptions(scriptOptions);
		}

		super.addTab(tab);
	}

	@Override
	protected void setup()
	{
		makeDialog();
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.itemeditor.entityname");
	}

	@Override
	protected String getWindowTitle()
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.itemeditor.title");
	}

	@Override
	public String getDocumentName()
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.itemeditor.name");
	}

	/**
	 * Setup the dialog and it's associated things.
	 */
	private void makeDialog()
	{
		// Basic setup stuff
		schema = new SchemaModel();
		scriptOptions = new ItemdefScriptOptions(clientService);
	}
}
