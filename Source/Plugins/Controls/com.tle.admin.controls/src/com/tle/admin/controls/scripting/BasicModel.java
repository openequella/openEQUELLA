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

package com.tle.admin.controls.scripting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dytech.edge.admin.script.InvalidScriptException;
import com.dytech.edge.admin.script.basicmodel.AbstractBasicModel;
import com.dytech.edge.admin.script.options.ScriptOptions;
import com.dytech.edge.admin.wizard.model.AbstractControlModel;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.control.WizardControlItem;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.NameValue;

public class BasicModel extends AbstractBasicModel
{
	private static final long serialVersionUID = 1L;
	private List<Control> controls;

	public BasicModel(SchemaModel schema, ScriptOptions options, String script, List<Control> controls)
		throws InvalidScriptException
	{
		super(schema, options, script);
		this.controls = controls;
	}

	public BasicModel(SchemaModel schema, ScriptOptions options, List<Control> controls)
	{
		super(schema, options);
		this.controls = controls;
	}

	@Override
	protected void defaultPopulateValues()
	{
		switch( xpathField.getType() )
		{
			case SCHEMA_ITEM:
				Collection<String> items = new ArrayList<String>();
				String node = xpathField.getSchemaXpath();

				for( Control control : getControls() )
				{
					if( control.getTargets().contains(node) && control instanceof AbstractControlModel<?> )
					{
						for( WizardControlItem item : ((AbstractControlModel<?>) control).getControl().getItems() )
						{
							items.add(item.getValue());
						}
					}
				}

				if( items.isEmpty() )
				{
					valueSelection.setEditable(true);
				}
				else
				{
					for( String item : items )
					{
						valueSelection.addItem(new NameValue(item, item));
					}
				}
				break;
			default:
				super.defaultPopulateValues();
		}
	}

	protected List<Control> getControls()
	{
		return controls;
	}

	public void setControls(List<Control> controls)
	{
		this.controls = controls;
	}

}
