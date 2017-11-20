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

package com.tle.admin.itemdefinition.mapping;

import java.awt.Component;
import java.util.EventListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SingleTargetChooser;

public class SchemaCellEditor extends AbstractCellEditor implements TableCellEditor
{
	private static final long serialVersionUID = 1L;
	private SingleTargetChooser chooser;

	public SchemaCellEditor(SchemaModel model)
	{
		chooser = new SingleTargetChooser(model, null);
		chooser.setOpaque(true);
	}

	public void setNonLeafSelection(boolean bool)
	{
		chooser.setNonLeafSelection(bool);
	}

	public void addCellEditedListener(CellEditedListener listener)
	{
		listenerList.add(CellEditedListener.class, listener);
	}

	private void fireCellEditing(int row, int column)
	{
		EventListener[] listeners = listenerList.getListeners(CellEditedListener.class);
		for( EventListener element : listeners )
		{
			((CellEditedListener) element).edited(this, row, column);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing
	 * .JTable, java.lang.Object, boolean, int, int)
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		fireCellEditing(row, column);

		chooser.setTarget((String) value);
		if( isSelected )
		{
			chooser.setBackground(table.getSelectionBackground());
			chooser.setForeground(table.getSelectionForeground());
		}
		else
		{
			chooser.setBackground(table.getBackground());
			chooser.setForeground(table.getForeground());
		}

		return chooser;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue()
	{
		return chooser.getTarget();
	}

	public interface CellEditedListener extends EventListener
	{
		void edited(SchemaCellEditor editor, int row, int column);
	}
}
