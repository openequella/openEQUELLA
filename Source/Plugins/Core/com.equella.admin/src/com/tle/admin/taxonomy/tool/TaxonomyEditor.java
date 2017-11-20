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

package com.tle.admin.taxonomy.tool;

import java.util.ArrayList;
import java.util.List;

import com.tle.admin.baseentity.AccessControlTab;
import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.taxonomy.Taxonomy;

/**
 * @author Nicholas Read
 */
public class TaxonomyEditor extends BaseEntityEditor<Taxonomy>
{
	public TaxonomyEditor(BaseEntityTool<Taxonomy> tool, boolean readonly)
	{
		super(tool, readonly);
	}

	@Override
	protected AbstractDetailsTab<Taxonomy> constructDetailsTab()
	{
		return new DetailsTab();
	}

	@Override
	protected List<BaseEntityTab<Taxonomy>> getTabs()
	{
		List<BaseEntityTab<Taxonomy>> tabs = new ArrayList<BaseEntityTab<Taxonomy>>();
		tabs.add((DetailsTab) detailsTab);
		tabs.add(new AccessControlTab<Taxonomy>(Node.TAXONOMY));
		return tabs;
	}

	@Override
	protected String getEntityName()
	{
		return getString("taxonomy.entityname"); //$NON-NLS-1$
	}

	@Override
	protected String getWindowTitle()
	{
		return getString("taxonomy.windowtitle"); //$NON-NLS-1$
	}

	@Override
	public String getDocumentName()
	{
		return getString("taxonomy.entityname"); //$NON-NLS-1$
	}
}
