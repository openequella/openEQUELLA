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

package com.tle.web.cloneormove.model;

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.schema.service.SchemaService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.Option;

/**
 * @author aholland
 */
public class SchemaTransformsModel extends DynamicHtmlListModel<String>
{
	private final SingleSelectionList<ItemDefinition> collections;
	private final SchemaService schemaService;
	private static String KEY_PFX = AbstractPluginService.getMyPluginId(SchemaTransformsModel.class)+".";


	public SchemaTransformsModel(SingleSelectionList<ItemDefinition> collections, SchemaService schemaService)
	{
		this.collections = collections;
		this.schemaService = schemaService;
	}

	@Override
	protected Iterable<String> populateModel(SectionInfo info)
	{
		ItemDefinition itemDef = collections.getSelectedValue(info);
		List<String> imports = null;
		if( itemDef != null )
		{
			Schema schema = itemDef.getSchema();
			if( schema != null )
			{
				imports = schemaService.getImportSchemaTypes(schema.getId());
			}
		}

		if( imports == null )
		{
			imports = new ArrayList<String>();
		}
		return imports;
	}

	@Override
	protected Option<String> getTopOption()
	{
		return new KeyOption<String>(KEY_PFX+"selectcollection.option.schematransform.none", "", null); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
