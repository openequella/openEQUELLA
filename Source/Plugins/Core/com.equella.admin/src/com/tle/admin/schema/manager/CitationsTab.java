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

package com.tle.admin.schema.manager;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.TableLayout;
import com.dytech.gui.file.FileFilterAdapter;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.gui.EditorException;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.schema.Citation;
import com.tle.common.adminconsole.FileUploader;
import com.tle.common.applet.client.DialogUtils;
import com.tle.common.applet.client.DialogUtils.DialogResult;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class CitationsTab extends BaseEntityTab<Schema> implements ActionListener, ListSelectionListener
{
	CitationTableModel model;

	JTable table;
	private JButton add;
	private JButton remove;

	@Override
	public void init(Component parent)
	{
		setupGUI();
	}

	@Override
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.schema.manager.citationtab.title"); //$NON-NLS-1$
	}

	private void setupGUI()
	{
		model = new CitationTableModel();
		table = new JTable(model);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);

		TableColumn c1 = table.getColumnModel().getColumn(1);
		c1.setCellEditor(new TransformCellEditor());

		TableColumn c0 = table.getColumnModel().getColumn(0);
		JComboBox box = new JComboBox(new String[]{"Harvard", "Chicago", "MLA", "NLA"});
		box.setEditable(true);
		c0.setCellEditor(new DefaultCellEditor(box));

		add = new JButton(CurrentLocale.get("com.tle.admin.schema.manager.citationtab.add"));
		remove = new JButton(CurrentLocale.get("com.tle.admin.schema.manager.citationtab.remove"));

		add.addActionListener(this);
		remove.addActionListener(this);

		JScrollPane tableScroll = new JScrollPane(table);

		final int height1 = add.getPreferredSize().height;
		final int width1 = remove.getPreferredSize().width;

		final int[] rows = {height1, height1, TableLayout.FILL,};
		final int[] cols = {TableLayout.FILL, width1,};

		setLayout(new TableLayout(rows, cols));

		add(tableScroll, new Rectangle(0, 0, 1, 3));
		add(add, new Rectangle(1, 0, 1, 1));
		add(remove, new Rectangle(1, 1, 1, 1));

		updateButtons();
	}

	public class TransformCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
	{
		private static final long serialVersionUID = 1L;
		private String currentValue;
		private JButton button;

		public TransformCellEditor()
		{
			button = new JButton();
			button.addActionListener(this);
			button.setBorderPainted(false);
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
				final Citation transform = model.get(table.getSelectedRow());

				final DialogResult result = DialogUtils
					.openDialog(getComponent(), null, FileFilterAdapter.XSLT(), null);
				if( result.isCancelled() )
				{
					return;
				}
				final File xslt = result.getFile();

				GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
				{
					@Override
					public Object construct() throws Exception
					{
						deleteXSLT(transform.getTransformation());
						uploadXSLT(xslt);

						transform.setTransformation(xslt.getName());

						return null;
					}

					@Override
					public void finished()
					{
						model.dataHasChanged();
					}

					@Override
					public void exception()
					{
						Exception ex = getException();
						LOGGER.error("Error deleting or uploading file", ex);
						Driver.displayError(getComponent(), "schema/upload.transform", ex); //$NON-NLS-1$
					}
				};
				worker.setComponent(getComponent());
				worker.start();
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
		 * javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax
		 * .swing.JTable, java.lang.Object, boolean, int, int)
		 */
		@Override
		public Component getTableCellEditorComponent(JTable table1, Object value, boolean isSelected, int row,
			int column)
		{
			currentValue = (String) value;
			return button;
		}
	}

	private void updateButtons()
	{
		boolean readonly = state.isReadonly();
		add.setEnabled(!readonly);

		boolean enable = !readonly && table.getSelectedRow() >= 0;
		remove.setEnabled(enable);
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
			onDelete();
		}
	}

	private void onAdd()
	{
		model.add(new Citation());
	}

	private void onDelete()
	{
		final int result = JOptionPane.showConfirmDialog(panel,
			"Are you sure you wish to delete the selected citation?");
		if( result != JOptionPane.YES_OPTION )
		{
			return;
		}

		final int index = table.getSelectedRow();
		final Citation transform = model.get(index);

		GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct() throws Exception
			{
				deleteXSLT(transform.getTransformation());
				return null;
			}

			@Override
			public void finished()
			{
				// Jira TLE-4703. Otherwise model/table gets confused when
				// trying to save after delete.
				table.editingStopped(null);
				model.remove(index);
			}

			@Override
			public void exception()
			{
				Exception ex = getException();
				LOGGER.error("Error deleting file", ex);
				Driver.displayError(getComponent(), "schema/delete.transform", ex); //$NON-NLS-1$
			}
		};
		worker.setComponent(panel);
		worker.start();
	}

	void uploadXSLT(File file) throws IOException
	{
		try( InputStream in = new BufferedInputStream(new FileInputStream(file)) )
		{
			FileUploader.upload(adminService, state.getEntityPack().getStagingID(), file.getName(), in);
		}
	}

	void deleteXSLT(String filename)
	{
		adminService.removeFile(state.getEntityPack().getStagingID(), filename);
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
		updateButtons();
	}

	public void addToChangeListener(ChangeDetector changeDetector)
	{
		changeDetector.watch(model);
	}

	@Override
	public void load()
	{
		model.load(state.getEntity());
	}

	@Override
	public void save()
	{
		// Nothing to save. Modifying bean directly.
		table.editingStopped(null);
	}

	@Override
	public void validation() throws EditorException
	{
		// DO NOTHING
	}

	private static class CitationTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;

		private String[] names;

		private List<Citation> citations;

		public CitationTableModel()
		{
			citations = new ArrayList<Citation>();
		}

		public void load(Schema schema)
		{
			citations = schema.getCitations();
			dataHasChanged();
		}

		public void add(Citation transform)
		{
			citations.add(transform);
			dataHasChanged();
		}

		public Citation get(int row)
		{
			return citations.get(row);
		}

		public void remove(int row)
		{
			citations.remove(row);
			dataHasChanged();
		}

		public void dataHasChanged()
		{
			fireTableDataChanged();
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount()
		{
			return getNames().length;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount()
		{
			return citations.size();
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			Citation citation = citations.get(rowIndex);
			if( columnIndex == 0 )
			{
				return citation.getName();
			}
			else
			{
				return citation.getTransformation();
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			Citation citation = citations.get(rowIndex);
			if( columnIndex == 0 )
			{
				citation.setName((String) aValue);
			}

		}

		@Override
		public String getColumnName(int column)
		{
			return getNames()[column];
		}

		private String[] getNames()
		{
			if( names == null )
			{
				names = new String[]{CurrentLocale.get("com.tle.admin.schema.manager.citationtab.column.citation"), //$NON-NLS-1$
						CurrentLocale.get("com.tle.admin.schema.manager.citationtab.column.transformation") //$NON-NLS-1$
				};
			}
			return names;
		}
	}
}
