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

package com.tle.common.accesscontrolbuilder;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class ActionTableCellEditor extends DefaultCellEditor
{
	private static final long serialVersionUID = 1L;
	private static final String GRANT = CurrentLocale.get("security.editor.grant"); //$NON-NLS-1$
	private static final String REVOKE = CurrentLocale.get("security.editor.revoke"); //$NON-NLS-1$

	public ActionTableCellEditor()
	{
		super(new JComboBox<>(new String[]{GRANT, REVOKE}));
	}

	@Override
	public Object getCellEditorValue()
	{
		return super.getCellEditorValue().equals(GRANT);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		String newValue = Boolean.TRUE.equals(value) ? GRANT : REVOKE;
		return super.getTableCellEditorComponent(table, newValue, isSelected, row, column);
	}
}