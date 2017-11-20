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

package com.dytech.edge.admin.wizard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.dytech.edge.wizard.beans.control.WizardControlItem;
import com.dytech.gui.ChangeDetector;
import com.dytech.gui.TableLayout;
import com.dytech.gui.adapters.TablePasteAdapter;
import com.dytech.gui.adapters.TablePasteModel;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.i18n.BundleCache;

public class DualShuffleList extends JComponent implements ActionListener
{
	private static final long serialVersionUID = 1L;
	protected static final String UP_ICON = "/icons/up.gif"; //$NON-NLS-1$
	protected static final String DOWN_ICON = "/icons/down.gif"; //$NON-NLS-1$

	protected JButton add;
	protected JButton remove;
	protected JButton up;
	protected JButton down;
	protected I18nTextField firstField;
	protected JTextField secondField;
	protected JTable table;
	protected MyTableModel model;

	public DualShuffleList(String firstText, String secondText)
	{
		setupGUI(firstText, secondText);
	}

	public void addItem(WizardControlItem item)
	{
		model.addItem(item);
	}

	public void clear()
	{
		int count = model.getRowCount();
		for( int i = 0; i < count; i++ )
		{
			model.removeRow(0);
		}
	}

	public List<WizardControlItem> getItems()
	{
		table.editingCanceled(new ChangeEvent(table));
		return model.getItems();
	}

	public void setItems(Collection<WizardControlItem> items)
	{
		clear();

		for( WizardControlItem item : items )
		{
			addItem(item);
		}
	}

	protected void setupGUI(String t1, String t2)
	{
		JLabel firstText = new JLabel(t1);
		JLabel secondText = new JLabel(t2);

		firstField = new I18nTextField(BundleCache.getLanguages());
		secondField = new JTextField();

		add = new JButton(CurrentLocale.get("com.dytech.edge.admin.wizard.dualshufflelist.add")); //$NON-NLS-1$
		remove = new JButton(CurrentLocale.get("com.dytech.edge.admin.wizard.dualshufflelist.remove")); //$NON-NLS-1$
		up = new JButton(new ImageIcon(DualShuffleList.class.getResource(UP_ICON)));
		down = new JButton(new ImageIcon(DualShuffleList.class.getResource(DOWN_ICON)));

		add.addActionListener(this);
		remove.addActionListener(this);
		up.addActionListener(this);
		down.addActionListener(this);

		model = new MyTableModel();

		TablePasteAdapter adapter = TablePasteAdapter.createTable(model);
		adapter.register(LanguageBundle.class, new XStreamDataConverter(LanguageBundle.class));

		table = adapter.getTable();
		table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column)
			{
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				setText(LangUtils.getString((LanguageBundle) value));
				return this;
			}
		});
		table.getColumnModel().getColumn(0).setCellEditor(new I18nCellEditor());

		JScrollPane scroller = new JScrollPane(table);
		scroller.getViewport().setBackground(Color.WHITE);

		int height1 = firstField.getPreferredSize().height;
		int height2 = secondField.getPreferredSize().height;
		int height3 = up.getPreferredSize().height;
		int width1 = remove.getPreferredSize().width;
		int width2 = Math.max(firstText.getPreferredSize().width, secondText.getPreferredSize().width) - width1;
		if( width2 < 0 )
		{
			width2 = 0;
		}

		int[] rows = new int[]{height1, height2, height3, height3, height3, height3};
		int[] columns = new int[]{width1, width2, TableLayout.FILL};
		setLayout(new TableLayout(rows, columns, 5, 5));

		add(firstText, new Rectangle(0, 0, 2, 1));
		add(firstField, new Rectangle(2, 0, 1, 1));
		add(secondText, new Rectangle(0, 1, 2, 1));
		add(secondField, new Rectangle(2, 1, 1, 1));

		add(add, new Rectangle(0, 2, 1, 1));
		add(remove, new Rectangle(0, 3, 1, 1));
		add(up, new Rectangle(0, 4, 1, 1));
		add(down, new Rectangle(0, 5, 1, 1));

		add(scroller, new Rectangle(1, 2, 2, 4));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		table.editingCanceled(new ChangeEvent(e.getSource()));

		if( e.getSource() == add )
		{
			String s2 = secondField.getText();
			if( !firstField.isCompletelyEmpty() && s2.length() > 0 )
			{
				WizardControlItem item = new WizardControlItem();
				item.setName(firstField.save());
				item.setValue(s2);
				model.addItem(item);
				firstField.load(null);
				secondField.setText(""); //$NON-NLS-1$
			}
		}
		else if( e.getSource() == remove )
		{
			int[] rows = table.getSelectedRows();
			for( int i = rows.length - 1; i >= 0; i-- )
			{
				model.removeRow(rows[i]);
			}
		}
		else if( e.getSource() == up )
		{
			if( table.getSelectedRowCount() != 0 )
			{
				int index = table.getSelectedRow();
				if( index > 0 )
				{
					WizardControlItem item = model.removeRow(index);
					model.insertItem(index - 1, item);
					table.updateUI();
					table.setRowSelectionInterval(index - 1, index - 1);
				}
			}
		}
		else if( e.getSource() == down && table.getSelectedRowCount() != 0 )
		{
			int index = table.getSelectedRow();
			if( index < model.getRowCount() - 1 )
			{
				WizardControlItem item = model.removeRow(index);
				model.insertItem(index + 1, item);
				table.updateUI();
				table.setRowSelectionInterval(index + 1, index + 1);
			}
		}
	}

	public void addChangeDetector(ChangeDetector changeDetector)
	{
		if( changeDetector != null )
		{
			changeDetector.watch(model);
		}
	}

	public void reload()
	{
		model.fireTableDataChanged();
	}

	public void capture()
	{
		if( table.getCellEditor() != null )
		{
			table.getCellEditor().stopCellEditing();
		}
	}

	@Override
	public void setEnabled(boolean enable)
	{
		super.setEnabled(enable);

		add.setEnabled(enable);
		remove.setEnabled(enable);
		up.setEnabled(enable);
		down.setEnabled(enable);
		firstField.setEnabled(enable);
		secondField.setEnabled(enable);
		table.setEnabled(enable);
	}

	public static class MyTableModel extends AbstractTableModel implements TablePasteModel
	{
		private static final long serialVersionUID = 1L;

		private static final String[] COLUMN_NAMES = new String[]{
				CurrentLocale.get("com.dytech.edge.admin.wizard.dualshufflelist.name"), //$NON-NLS-1$
				CurrentLocale.get("com.dytech.edge.admin.wizard.dualshufflelist.value")}; //$NON-NLS-1$

		private final List<WizardControlItem> items = new ArrayList<WizardControlItem>();

		public MyTableModel()
		{
			super();
		}

		public void addItem(WizardControlItem item)
		{
			insertItem(items.size(), item);
		}

		public void insertItem(int index, WizardControlItem item)
		{
			items.add(index, item);
			fireTableRowsInserted(index, index);
		}

		@Override
		public void insertRow(int row, List<?> data)
		{
			WizardControlItem item = new WizardControlItem();
			item.setName((LanguageBundle) data.get(0));
			item.setValue((String) data.get(1));
			insertItem(row, item);
		}

		public WizardControlItem removeRow(int index)
		{
			WizardControlItem item = items.remove(index);
			fireTableRowsDeleted(index, index);
			return item;
		}

		@Override
		public int getColumnCount()
		{
			return 2;
		}

		@Override
		public String getColumnName(int column)
		{
			return COLUMN_NAMES[column];
		}

		@Override
		public int getRowCount()
		{
			if( items == null )
			{
				return 0;
			}
			return items.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			WizardControlItem item = items.get(rowIndex);
			switch( columnIndex )
			{
				case 0:
					return item.getName();
				case 1:
					return item.getValue();
				default:
					throw new IllegalStateException();
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			WizardControlItem item = items.get(rowIndex);
			switch( columnIndex )
			{
				case 0:
					item.setName((LanguageBundle) aValue);
					break;
				case 1:
					item.setValue((String) aValue);
					break;

				default:
					break;
			}

			fireTableDataChanged();
		}

		public List<WizardControlItem> getItems()
		{
			return items;
		}

		@Override
		public Class<?> getColumnClass(int column)
		{
			if( column == 0 )
			{
				return LanguageBundle.class;
			}
			else
			{
				return super.getColumnClass(column);
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return true;
		}
	}
}
