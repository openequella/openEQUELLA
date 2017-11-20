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

package com.tle.admin.schema.manager;

import java.util.ArrayList;
import java.util.List;

import com.tle.admin.baseentity.AccessControlTab;
import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.entity.Schema;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;

/**
 * @author Nicholas Read
 */
public class SchemaManager extends BaseEntityEditor<Schema>
{
	private SchemaModel schemaModel;
	private EditorTab editorTab;

	/**
	 * Constructs a new SchemaManager.
	 */
	public SchemaManager(BaseEntityTool<Schema> tool, boolean readonly)
	{
		super(tool, readonly);
	}

	@Override
	protected String getEntityName()
	{
		return "metadata schema";
	}

	@Override
	public String getDocumentName()
	{
		return CurrentLocale.get("com.tle.admin.schema.manager.schemamanager.docname");
	}

	@Override
	protected String getWindowTitle()
	{
		return CurrentLocale.get("com.tle.admin.schema.manager.schemamanager.title");
	}

	@Override
	protected void setup()
	{
		schemaModel = new SchemaModel();
	}

	@Override
	protected List<BaseEntityTab<Schema>> getTabs()
	{
		editorTab = new EditorTab(schemaModel);

		List<BaseEntityTab<Schema>> tabs = new ArrayList<BaseEntityTab<Schema>>();
		tabs.add((DetailsTab) detailsTab);
		tabs.add(editorTab);
		tabs.add(new TransformationsTab());
		tabs.add(new CitationsTab());
		tabs.add(new AccessControlTab<Schema>(Node.SCHEMA));
		return tabs;
	}

	@Override
	protected List<? extends BaseEntityTab<Schema>> getTabLoadOrder(List<? extends BaseEntityTab<Schema>> btabs2)
	{
		List<BaseEntityTab<Schema>> results = new ArrayList<BaseEntityTab<Schema>>(btabs2);
		results.remove(editorTab);
		results.add(0, editorTab);
		return results;
	}

	@Override
	protected AbstractDetailsTab<Schema> constructDetailsTab()
	{
		return new DetailsTab(schemaModel);
	}
}
