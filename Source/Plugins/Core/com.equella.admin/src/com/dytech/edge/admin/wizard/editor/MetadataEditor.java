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

package com.dytech.edge.admin.wizard.editor;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.admin.wizard.model.MetadataModel;
import com.dytech.edge.wizard.beans.Metadata;
import com.dytech.gui.TableLayout;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SingleTargetChooser;
import com.tle.common.i18n.CurrentLocale;

public class MetadataEditor extends Editor implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private SingleTargetChooser name;
	private JTextField metadata;

	private JButton addButton;
	private JButton removeButton;
	private MyTableModel model;
	private JTable table;

	public MetadataEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
		setupGUI();
	}

	@Override
	protected void loadControl()
	{
		MetadataModel control = (MetadataModel) getControl();
		model.setRows(control.getMetadata());
	}

	@Override
	protected void saveControl()
	{
		MetadataModel control = (MetadataModel) getControl();

		control.getMetadata().clear();
		control.getMetadata().addAll(model.getRows());
	}

	private void setupGUI()
	{
		setShowScripting(true);

		JLabel nameLabel = new JLabel(
			CurrentLocale.get("com.dytech.edge.admin.wizard.editor.metadateeditor.metatarget")); //$NON-NLS-1$
		JLabel metadataLabel = new JLabel(
			CurrentLocale.get("com.dytech.edge.admin.wizard.editor.metadateeditor.metavalue")); //$NON-NLS-1$

		name = WizardHelper.createSingleTargetChooser(this);
		metadata = new JTextField();
		addButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.metadateeditor.add")); //$NON-NLS-1$
		removeButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.metadateeditor.remove")); //$NON-NLS-1$

		model = new MyTableModel();
		TableSorter sorter = new TableSorter(model);
		table = new JTable(sorter);
		sorter.setTableHeader(table.getTableHeader());

		JScrollPane tableScroll = new JScrollPane(table);
		tableScroll.getViewport().setBackground(Color.WHITE);

		addButton.addActionListener(this);
		removeButton.addActionListener(this);

		final int width1 = nameLabel.getPreferredSize().width;
		final int height2 = name.getPreferredSize().height;
		final int height3 = metadata.getPreferredSize().height;
		final int height4 = addButton.getPreferredSize().height;

		final int[] rows = {height2, height3, 10, height4, height4, 150, TableLayout.FILL,};
		final int[] cols = {width1, TableLayout.FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols));

		all.add(nameLabel, new Rectangle(0, 0, 1, 1));
		all.add(name, new Rectangle(1, 0, 1, 1));

		all.add(metadataLabel, new Rectangle(0, 1, 1, 1));
		all.add(metadata, new Rectangle(1, 1, 1, 1));

		all.add(addButton, new Rectangle(0, 3, 1, 1));
		all.add(removeButton, new Rectangle(0, 4, 1, 1));
		all.add(tableScroll, new Rectangle(1, 3, 1, 3));

		addSection(all);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == addButton )
		{
			String xpath = name.getTarget();
			String value = metadata.getText();

			if( xpath.length() > 0 )
			{
				model.addRow(xpath, value);
				name.setTarget(""); //$NON-NLS-1$
				metadata.setText(""); //$NON-NLS-1$
			}
		}
		else if( e.getSource() == removeButton )
		{
			int[] indices = table.getSelectedRows();
			for( int i = indices.length - 1; i >= 0; i-- )
			{
				model.removeRow(indices[i]);
			}
		}
	}

	/**
	 * @author Nicholas Read
	 */
	private static class MyTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;
		private List<Metadata> rows = new ArrayList<Metadata>();

		public MyTableModel()
		{
			super();
		}

		public void addRow(String xpath, String value)
		{
			rows.add(new Metadata(xpath, value));

			int index = rows.size() - 1;
			fireTableRowsInserted(index, index);
		}

		public void removeRow(int index)
		{
			rows.remove(index);
			fireTableRowsDeleted(index, index);
		}

		public List<Metadata> getRows()
		{
			return rows;
		}

		public void setRows(List<Metadata> rows)
		{
			this.rows.clear();
			this.rows.addAll(rows);
			fireTableDataChanged();
		}

		@Override
		public int getColumnCount()
		{
			return 2;
		}

		@Override
		public int getRowCount()
		{
			return rows.size();
		}

		@Override
		public String getColumnName(int column)
		{
			if( column == 0 )
			{
				return CurrentLocale.get("com.dytech.edge.admin.wizard.editor.metadateeditor.item"); //$NON-NLS-1$
			}
			else
			{
				return CurrentLocale.get("com.dytech.edge.admin.wizard.editor.metadateeditor.value"); //$NON-NLS-1$
			}
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			Metadata row = rows.get(rowIndex);
			if( columnIndex == 0 )
			{
				return row.getTarget();
			}
			else
			{
				return row.getValue();
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columnIndex == 1;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			if( columnIndex != 1 )
			{
				throw new RuntimeException();
			}

			Metadata row = rows.get(rowIndex);
			row.setValue((String) aValue);
		}
	}
}
