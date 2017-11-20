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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.dytech.edge.admin.script.ScriptEditor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.gui.TableLayout;
import com.tle.admin.controls.scripting.BasicModel;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class ScriptedLiterals extends JComponent implements ActionListener, ListSelectionListener
{
	private static final long serialVersionUID = 1L;
	private static final String UP_ICON = "/icons/up.gif"; //$NON-NLS-1$
	private static final String DOWN_ICON = "/icons/down.gif"; //$NON-NLS-1$

	private JLabel title;
	private MyTableModel model;
	private JTable table;
	private JScrollPane scroller;
	private JButton add;
	private JButton remove;
	private JButton up;
	private JButton down;

	/**
	 * Constructs a new ScriptedLiterals component.
	 */
	public ScriptedLiterals(SchemaModel schema)
	{
		super();
		createGUI(schema);
	}

	/**
	 * Constructs the GUI for this component. This should only be called once.
	 */
	private void createGUI(SchemaModel schema)
	{
		title = new JLabel(" "); //$NON-NLS-1$

		add = new JButton(CurrentLocale.get("com.tle.admin.add")); //$NON-NLS-1$
		remove = new JButton(CurrentLocale.get("com.tle.admin.remove")); //$NON-NLS-1$
		up = new JButton(new ImageIcon(ScriptedLiterals.class.getResource(UP_ICON)));
		down = new JButton(new ImageIcon(ScriptedLiterals.class.getResource(DOWN_ICON)));

		add.addActionListener(this);
		remove.addActionListener(this);
		up.addActionListener(this);
		down.addActionListener(this);

		model = new MyTableModel();
		table = new JTable(model);
		scroller = new JScrollPane(table);

		table.setRowHeight((int) (table.getRowHeight() * 1.5));
		table.getSelectionModel().addListSelectionListener(this);

		TableColumn scriptColumn = table.getColumnModel().getColumn(1);
		scriptColumn.setCellEditor(new MyCellEditor(this, schema));
		scriptColumn.setCellRenderer(new MyCellRenderer());

		final int width1 = up.getPreferredSize().width;
		final int width2 = remove.getPreferredSize().width;
		final int height1 = title.getPreferredSize().height;
		final int height2 = up.getPreferredSize().height;
		final int height3 = add.getPreferredSize().height;

		final int[] rows = {height1, TableLayout.FILL, height2, height2, TableLayout.FILL, height3, height3,};
		final int[] cols = {width1, width2, width2, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));
		add(title, new Rectangle(1, 0, 3, 1));
		add(scroller, new Rectangle(1, 1, 3, 4));
		add(up, new Rectangle(0, 2, 1, 1));
		add(down, new Rectangle(0, 3, 1, 1));
		add(add, new Rectangle(1, 5, 1, 1));
		add(remove, new Rectangle(2, 5, 1, 1));

		loadTarget(null);
	}

	/**
	 * @param target The target to load, or null if none.
	 */
	public void loadTarget(ScriptedTarget target)
	{
		setTitle(target);

		boolean enabled = target != null;
		title.setEnabled(enabled);
		table.setEnabled(enabled);
		scroller.setEnabled(enabled);
		add.setEnabled(enabled);
		remove.setEnabled(enabled);
		up.setEnabled(enabled);
		down.setEnabled(enabled);

		if( target != null )
		{
			model.setRules(target.getRules());
			updateButtons();
		}
		else
		{
			model.setRules(null);
		}
	}

	/**
	 * This ensures that all buttons are properly enalbed or disabled for the
	 * current state.
	 */
	private void updateButtons()
	{
		int index = table.getSelectedRow();
		int lastIndex = model.getRowCount() - 1;

		add.setEnabled(true);
		remove.setEnabled(index >= 0);
		up.setEnabled(index > 0);
		down.setEnabled(index >= 0 && index < lastIndex);
	}

	/**
	 * Updates the title label
	 */
	private void setTitle(ScriptedTarget target)
	{
		if( target == null )
		{
			title.setText(CurrentLocale.get("com.tle.admin.itemdefinition.mapping.scriptedliterals.noschema")); //$NON-NLS-1$
		}
		else
		{
			title.setText(CurrentLocale.get("com.tle.admin.itemdefinition.mapping.scriptedliterals.literals", target //$NON-NLS-1$
				.getTarget()));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
	 * .ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if( e.getSource() == table.getSelectionModel() )
		{
			updateButtons();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == add )
		{
			onAdd();
		}
		else if( e.getSource() == remove )
		{
			onRemove();
		}
		else if( e.getSource() == up )
		{
			onUp();
		}
		else if( e.getSource() == down )
		{
			onDown();
		}
	}

	/**
	 * Invoke this to add a new literal.
	 */
	private void onAdd()
	{
		String literal = JOptionPane.showInputDialog(this,
			CurrentLocale.get("com.tle.admin.itemdefinition.mapping.scriptedliterals.enter")); //$NON-NLS-1$
		if( literal != null )
		{
			model.addRow(literal, ""); //$NON-NLS-1$
			int lastIndex = model.getRowCount() - 1;
			table.getSelectionModel().setSelectionInterval(lastIndex, lastIndex);
		}
	}

	/**
	 * Invoke this to remove the selected literal.
	 */
	private void onRemove()
	{
		int index = table.getSelectedRow();
		if( index >= 0 )
		{
			String literal = model.getLiteral(index);
			int result = JOptionPane.showConfirmDialog(this,
				CurrentLocale.get("com.tle.admin.itemdefinition.mapping.scriptedliterals.confirm", literal), //$NON-NLS-1$
				"Remove?", JOptionPane.YES_NO_OPTION);
			if( result == JOptionPane.YES_OPTION )
			{
				model.removeRow(index);
			}
		}
	}

	/**
	 * Invoke this to move the selected literal up one position.
	 */
	private void onUp()
	{
		int index = table.getSelectedRow();
		if( index > 0 )
		{
			model.swapRows(index - 1, index);
			table.getSelectionModel().setSelectionInterval(index - 1, index - 1);
		}
	}

	/**
	 * Invoke this to move the selected literal down one position.
	 */
	private void onDown()
	{
		int index = table.getSelectedRow();
		int lastIndex = model.getRowCount() - 1;
		if( index < lastIndex )
		{
			model.swapRows(index, index + 1);
			table.getSelectionModel().setSelectionInterval(index + 1, index + 1);
		}
	}

	/**
	 * Provides our customised table model for viewing Collections of
	 * ScriptedRules.
	 * 
	 * @author Nicholas Read
	 */
	private static class MyTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Our table column names.
		 */
		private static final String[] COLUMNS = {
				CurrentLocale.get("com.tle.admin.itemdefinition.mapping.scriptedliterals.literal"), //$NON-NLS-1$
				CurrentLocale.get("com.tle.admin.itemdefinition.mapping.scriptedliterals.script")}; //$NON-NLS-1$

		/**
		 * Our table data.
		 */
		private List<ScriptedRule> rules;

		/**
		 * Constructs a new MyTableModel.
		 */
		public MyTableModel()
		{
			super();
		}

		/**
		 * Sets a new list of literals for the table. Modifications to this
		 * model will be reflected in the given list.
		 * 
		 * @param rules List of ScriptedRule objects.
		 */
		public void setRules(List<ScriptedRule> rules)
		{
			this.rules = rules;
			fireTableDataChanged();
		}

		@Override
		public String getColumnName(int column)
		{
			return COLUMNS[column];
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount()
		{
			return COLUMNS.length;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount()
		{
			if( rules == null )
			{
				return 0;
			}
			else
			{
				return rules.size();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			ScriptedRule rule = rules.get(rowIndex);
			switch( columnIndex )
			{
				case 0:
					return rule.getLiteral();

				case 1:
					return rule.getScript();

				default:
					throw new IllegalArgumentException("Column " + columnIndex + " does not exist");
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return true;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			ScriptedRule rule = rules.get(rowIndex);
			switch( columnIndex )
			{
				case 0:
					rule.setLiteral((String) aValue);
					break;

				case 1:
					rule.setScript((String) aValue);
					break;

				default:
					throw new IllegalArgumentException("Column " + columnIndex + " does not exist");
			}
		}

		/**
		 * Removs the given row.
		 * 
		 * @param index index of the row to remove.
		 */
		public void removeRow(int index)
		{
			rules.remove(index);
			fireTableRowsDeleted(index, index);
		}

		/**
		 * Gets the literal value for the given row.
		 * 
		 * @param index row index for literal being requested.
		 * @return the literal value.
		 */
		public String getLiteral(int index)
		{
			return (String) getValueAt(index, 0);
		}

		/**
		 * Adds a new row to the model.
		 * 
		 * @param literal the literal value for the new row.
		 * @param script the script for the new row.
		 */
		public void addRow(String literal, String script)
		{
			if( literal == null || script == null )
			{
				throw new IllegalArgumentException("Neither parameter can be null");
			}

			ScriptedRule rule = new ScriptedRule();
			rule.setLiteral(literal);
			rule.setScript(script);

			rules.add(rule);
			int index = rules.size() - 1;
			fireTableRowsInserted(index, index);
		}

		/**
		 * Swaps the given rows.
		 * 
		 * @param i the first row.
		 * @param j the second row.
		 */
		public void swapRows(int i, int j)
		{
			Collections.swap(rules, i, j);
			fireTableRowsUpdated(i, j);
		}
	}

	private static class MyCellRenderer extends JButton implements TableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		public MyCellRenderer()
		{
			super();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent
		 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column)
		{
			String script = (String) value;
			if( script != null && script.length() > 0 )
			{
				setText(CurrentLocale.get("com.tle.admin.itemdefinition.mapping.scriptedliterals.edit")); //$NON-NLS-1$
			}
			else
			{
				setText(CurrentLocale.get("com.tle.admin.itemdefinition.mapping.scriptedliterals.add")); //$NON-NLS-1$
			}

			return this;
		}
	}

	private static class MyCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
	{
		private static final long serialVersionUID = 1L;
		private Component parent;
		private SchemaModel schema;
		private String script;
		private JButton button;

		public MyCellEditor(Component parent, SchemaModel schema)
		{
			this.parent = parent;
			this.schema = schema;

			button = new JButton();
			button.setOpaque(true);
			button.addActionListener(this);
			button.setText(CurrentLocale.get("com.tle.admin.itemdefinition.mapping.scriptedliterals.open")); //$NON-NLS-1$
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax
		 * .swing.JTable, java.lang.Object, boolean, int, int)
		 */
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
		{
			script = (String) value;
			return button;
		}

		@Override
		public Object getCellEditorValue()
		{
			return script;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if( e.getSource() == button )
			{
				List<Control> controls = Collections.emptyList();
				BasicModel model = new BasicModel(schema, null, controls);

				ScriptEditor s = new ScriptEditor(model);
				s.importScript(script);
				s.showEditor(parent);

				if( s.scriptWasSaved() )
				{
					script = s.getScript();
					fireEditingStopped();
				}
				else
				{
					fireEditingCanceled();
				}
			}
		}
	}
}
