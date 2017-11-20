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
		return getString("taxonomy.entityname"); //$NON-NLS-1$
	}
}
