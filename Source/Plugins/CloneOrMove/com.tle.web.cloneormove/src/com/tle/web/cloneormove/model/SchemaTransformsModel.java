package com.tle.web.cloneormove.model;

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.core.schema.SchemaService;
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
		return new KeyOption<String>("com.tle.web.cloneormove.selectcollection.option.schematransform.none", "", null); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
