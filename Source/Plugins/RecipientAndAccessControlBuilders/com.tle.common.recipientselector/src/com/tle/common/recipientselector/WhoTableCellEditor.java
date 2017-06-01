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

package com.tle.common.recipientselector;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.tle.common.Pair;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class WhoTableCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
{
	private static final long serialVersionUID = 1L;

	private final Component parent;

	private String currentValue;
	private JButton button;
	private ExpressionBuilderFinder finder;
	private SingleFinderDialog dialog;

	public WhoTableCellEditor(RemoteUserService userService, Component parent)
	{
		this.parent = parent;

		button = new JButton();
		button.addActionListener(this);
		button.setBorderPainted(false);

		finder = new ExpressionBuilderFinder(userService);
		dialog = new SingleFinderDialog(finder);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == button )
		{
			dialog.clearAll();
			try
			{
				finder.setExpression(currentValue);
			}
			catch( Exception ex )
			{
				JOptionPane.showMessageDialog(parent, "Previous expression was invalid.  Starting fresh.", "Error",
					JOptionPane.ERROR_MESSAGE);
			}
			Pair<RecipientFilter, Object> result = dialog.showFinder(parent);

			if( result != null )
			{
				currentValue = RecipientUtils.convertToRecipient(result.getFirst(), result.getSecond());
			}
			fireEditingStopped();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue()
	{
		return currentValue;
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
		currentValue = (String) value;
		return button;
	}
}