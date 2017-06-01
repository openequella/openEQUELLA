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

package com.tle.admin.search.searchset.scripting;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class ScriptingTableCellRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 1L;
	private final ScriptingTableModelInterface<?> model;

	public ScriptingTableCellRenderer(ScriptingTableModelInterface<?> model)
	{
		this.model = model;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
		int row, int column)
	{
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if( Check.isEmpty((String) value) )
		{
			setText(CurrentLocale.get("com.tle.admin.gui.common.cell.scriptingtablecellrenderer.add"));
		}
		else
		{
			setText(CurrentLocale.get("com.tle.admin.gui.common.cell.scriptingtablecellrenderer.edit"));
		}
		setEnabled(model.isScriptingEnabled(row));
		return this;
	}
}