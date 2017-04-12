/*
 * Created on Oct 7, 2005
 */

package com.tle.admin.itemdefinition;

import com.dytech.edge.admin.script.options.ItemdefScriptOptions;
import com.dytech.edge.admin.script.options.ScriptOptions;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.itemdef.ItemDefinition;

public abstract class AbstractItemdefTab extends BaseEntityTab<ItemDefinition>
{
	protected SchemaModel schema;
	protected ScriptOptions script;
	protected ItemdefScriptOptions options;

	public void setSchemaModel(SchemaModel schema)
	{
		this.schema = schema;
	}

	public void setOptions(ItemdefScriptOptions options)
	{
		this.options = options;
	}

	public void setScript(ScriptOptions script)
	{
		this.script = script;
	}
}
