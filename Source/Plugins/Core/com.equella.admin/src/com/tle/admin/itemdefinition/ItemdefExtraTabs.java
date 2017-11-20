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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.dytech.edge.admin.script.options.ItemdefScriptOptions;
import com.dytech.edge.admin.script.options.ScriptOptions;
import com.dytech.gui.JStatusBar;
import com.tle.admin.Driver;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.baseentity.EditorState;
import com.tle.admin.gui.EditorException;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.applet.gui.AppletGuiUtils;

public class ItemdefExtraTabs extends AbstractItemdefTab
{
	private final List<BaseEntityTab<ItemDefinition>> extra;
	private final String title;

	public ItemdefExtraTabs(String title)
	{
		this.title = title;
		extra = new ArrayList<BaseEntityTab<ItemDefinition>>();
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	public void add(BaseEntityTab<ItemDefinition> tab)
	{
		extra.add(tab);
	}

	@Override
	public void setState(EditorState<ItemDefinition> state)
	{
		for( BaseEntityTab<ItemDefinition> t : extra )
		{
			t.setState(state);
		}
	}

	@Override
	public void init(Component parent)
	{
		for( BaseEntityTab<ItemDefinition> t : extra )
		{
			t.init(parent);
		}
	}

	@Override
	public void load()
	{
		for( BaseEntityTab<ItemDefinition> t : extra )
		{
			t.load();
		}
	}

	@Override
	public void save()
	{
		for( BaseEntityTab<ItemDefinition> t : extra )
		{
			t.save();
		}
	}

	@Override
	public void validation() throws EditorException
	{
		for( BaseEntityTab<ItemDefinition> t : extra )
		{
			t.validation();
		}
	}

	@Override
	public JPanel getComponent()
	{
		JPanel temp = new JPanel(new BorderLayout());
		JTabbedPane p = new JTabbedPane();
		for( BaseEntityTab<ItemDefinition> t : extra )
		{
			JComponent comp = t.getComponent();
			comp.setBorder(AppletGuiUtils.DEFAULT_BORDER);
			p.add(comp, t.getTitle());
		}
		temp.add(p);
		return temp;
	}

	@Override
	public void setSchemaModel(SchemaModel schema)
	{
		for( BaseEntityTab<ItemDefinition> t : extra )
		{
			if( t instanceof AbstractItemdefTab )
			{
				((AbstractItemdefTab) t).setSchemaModel(schema);
			}
		}
	}

	@Override
	public void setOptions(ItemdefScriptOptions options)
	{
		for( BaseEntityTab<ItemDefinition> t : extra )
		{
			if( t instanceof AbstractItemdefTab )
			{
				((AbstractItemdefTab) t).setOptions(options);
			}
		}
	}

	@Override
	public void setDriver(Driver driver)
	{
		for( BaseEntityTab<ItemDefinition> t : extra )
		{
			t.setDriver(driver);
		}
	}

	@Override
	public void setParent(JDialog parent)
	{
		for( BaseEntityTab<ItemDefinition> t : extra )
		{
			t.setParent(parent);
		}
	}

	@Override
	public void setScript(ScriptOptions script)
	{
		for( BaseEntityTab<ItemDefinition> t : extra )
		{
			if( t instanceof AbstractItemdefTab )
			{
				((AbstractItemdefTab) t).setScript(script);
			}
		}
	}

	@Override
	public void setStatusBar(JStatusBar statusBar)
	{
		for( BaseEntityTab<ItemDefinition> t : extra )
		{
			t.setStatusBar(statusBar);
		}
	}
}