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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.dytech.gui.ChangeDetector;
import com.tle.admin.gui.common.JChangeDetectorPanel;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.itemdef.MetadataMapping;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Charles O'Farrell
 */
public abstract class AbstractTableMapping<MAPPING> extends JChangeDetectorPanel
	implements
		Mapping,
		ListSelectionListener,
		ActionListener
{
	private JTable table;
	protected DefaultTableModel model;
	protected SchemaModel schema;
	private JButton addButton;
	private JButton removeButton;

	public AbstractTableMapping(SchemaModel schema)
	{
		this.schema = schema;
		init();
	}

	private void init()
	{
		model = new DefaultTableModel()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int c)
			{
				Object value = getValueAt(0, c);
				Class<?> cs = null;
				if( value != null )
				{
					cs = value.getClass();
				}
				else
				{
					cs = super.getColumnClass(c);
				}
				return cs;
			}
		};
		model.setColumnIdentifiers(getColumnNames());
		table = new JTable(model);
		table.getSelectionModel().addListSelectionListener(this);
		table.setRowHeight(20);

		int count = model.getColumnCount();
		for( int i = 0; i < count; i++ )
		{
			TableColumn column = table.getColumnModel().getColumn(i);
			processTableColumn(column, i);
		}
		table.doLayout();

		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(table), BorderLayout.CENTER);
		this.add(createBottom(), BorderLayout.SOUTH);
	}

	protected void processTableColumn(TableColumn column, int col)
	{
		column.setCellEditor(getCellEditor(col));
	}

	private JPanel createBottom()
	{
		addButton = new JButton(CurrentLocale.get("com.tle.admin.add")); //$NON-NLS-1$
		removeButton = new JButton(CurrentLocale.get("com.tle.admin.remove")); //$NON-NLS-1$

		addButton.addActionListener(this);
		removeButton.addActionListener(this);

		updateButtons();

		JPanel all = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		all.add(addButton);
		all.add(removeButton);
		return all;
	}

	public void add()
	{
		model.addRow(new String[]{"", ""}); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@SuppressWarnings("nls")
	public void remove()
	{
		int result = JOptionPane.showConfirmDialog(this,
			CurrentLocale.get("com.tle.admin.itemdefinition.mapping.AbstractTableMapping.remove.message"),
			CurrentLocale.get("com.tle.admin.itemdefinition.mapping.AbstractTableMapping.remove.title"),
			JOptionPane.YES_NO_OPTION);
		if( result == JOptionPane.YES_OPTION )
		{
			table.editingStopped(null);

			int[] rows = table.getSelectedRows();
			for( int i = rows.length - 1; i >= 0; i-- )
			{
				model.removeRow(rows[i]);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.edge.admin.itemdefinition.mapping.Mapping#setChangeDetector
	 * (com.dytech.gui.ChangeDetector)
	 */
	public void setChangeDetector(ChangeDetector detector)
	{
		detector.watch(model);
	}

	@Override
	public JComponent getComponent()
	{
		return this;
	}

	@Override
	public void save(MetadataMapping mapping)
	{
		table.editingStopped(null);

		Collection<MAPPING> col = getMappings(mapping);
		col.clear();
		int rowcount = model.getRowCount();
		int colcount = model.getColumnCount();
		for( int i = 0; i < rowcount; i++ )
		{
			MAPPING o = newMapping();
			for( int j = 0; j < colcount; j++ )
			{
				String value = model.getValueAt(i, j).toString();
				setNode(o, j, value);
			}
			col.add(o);
		}
	}

	@Override
	public void loadItem(MetadataMapping mapping)
	{
		if( mapping != null )
		{
			Collection<MAPPING> col = getMappings(mapping);
			for( MAPPING map : col )
			{
				Object[] data = new Object[getColumnNames().length];
				for( int j = 0; j < data.length; j++ )
				{
					data[j] = getNode(map, j);
				}
				model.addRow(data);
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		updateButtons();
	}

	private void updateButtons()
	{
		removeButton.setEnabled(table.getSelectedRow() >= 0);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == addButton )
		{
			add();
		}
		else if( e.getSource() == removeButton )
		{
			remove();
		}
	}

	protected abstract MAPPING newMapping();

	protected abstract void setNode(Object object, int column, String value);

	protected abstract Object getNode(Object data, int column);

	protected abstract String[] getColumnNames();

	protected abstract Collection<MAPPING> getMappings(MetadataMapping mapping);

	protected abstract TableCellEditor getCellEditor(int column);
}
