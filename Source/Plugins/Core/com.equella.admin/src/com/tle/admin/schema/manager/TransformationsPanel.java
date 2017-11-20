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

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.baseentity.EditorState;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.SchemaTransform;
import com.tle.common.Check;
import com.tle.common.adminconsole.FileUploader;
import com.tle.common.adminconsole.RemoteAdminService;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class TransformationsPanel extends JPanel implements ActionListener, ListSelectionListener
{
	private static final Log LOGGER = LogFactory.getLog(TransformationsPanel.class);

	private final RemoteAdminService adminService;
	private final EditorState<Schema> state;
	private final boolean handleImports;

	private final MyTableModel model;
	private final JTable table;
	private final JButton add;
	private final JButton edit;
	private final JButton remove;

	public TransformationsPanel(RemoteAdminService adminService, EditorState<Schema> state, boolean handleImports)
	{
		this.adminService = adminService;
		this.state = state;
		this.handleImports = handleImports;

		model = new MyTableModel();
		table = new JTable(model);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);

		add = new JButton(CurrentLocale.get("com.tle.admin.add")); //$NON-NLS-1$
		edit = new JButton(CurrentLocale.get("com.tle.admin.edit")); //$NON-NLS-1$
		remove = new JButton(CurrentLocale.get("com.tle.admin.remove")); //$NON-NLS-1$

		add.addActionListener(this);
		edit.addActionListener(this);
		remove.addActionListener(this);

		JScrollPane tableScroll = new JScrollPane(table);

		final int height1 = add.getPreferredSize().height;
		final int width1 = remove.getPreferredSize().width;

		final int[] rows = {height1, height1, height1, TableLayout.FILL,};
		final int[] cols = {TableLayout.FILL, width1,};

		setLayout(new TableLayout(rows, cols));

		add(tableScroll, new Rectangle(0, 0, 1, 4));
		add(add, new Rectangle(1, 0, 1, 1));
		add(edit, new Rectangle(1, 1, 1, 1));
		add(remove, new Rectangle(1, 2, 1, 1));

		updateButtons();
	}

	private void updateButtons()
	{
		boolean readonly = state.isReadonly();
		add.setEnabled(!readonly);

		boolean enable = !readonly && table.getSelectedRow() >= 0;
		edit.setEnabled(enable);
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
		else if( e.getSource() == edit )
		{
			onEdit();
		}
		else if( e.getSource() == remove )
		{
			onDelete();
		}
	}

	private void onAdd()
	{
		final TransformDialog dialog = new TransformDialog();
		if( !dialog.showEditDialog(this, null) )
		{
			return;
		}

		GlassSwingWorker<SchemaTransform> worker = new GlassSwingWorker<SchemaTransform>()
		{
			@Override
			public SchemaTransform construct() throws IOException
			{
				File xslt = dialog.getSelectedFile();
				uploadXSLT(xslt);

				SchemaTransform transform = new SchemaTransform();
				transform.setType(dialog.getSchemaType());
				transform.setFilename(dialog.getSelectedFile().getName());
				return transform;
			}

			@Override
			public void finished()
			{
				model.add(get());
			}

			@Override
			public void exception()
			{
				Exception ex = getException();
				LOGGER.error("Error uploading file", ex);
				Driver.displayError(getComponent(), "schema/upload.transform", ex); //$NON-NLS-1$
			}
		};
		worker.setComponent(this);
		worker.start();
	}

	private void onEdit()
	{
		final SchemaTransform transform = model.get(table.getSelectedRow());

		final TransformDialog dialog = new TransformDialog();
		if( !dialog.showEditDialog(this, transform) )
		{
			return;
		}

		final File xslt = dialog.getSelectedFile();

		GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct() throws IOException
			{
				deleteXSLT(transform.getFilename());
				uploadXSLT(xslt);

				transform.setType(dialog.getSchemaType());
				transform.setFilename(dialog.getSelectedFile().getName());

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
		worker.setComponent(this);
		worker.start();
	}

	private void onDelete()
	{
		final int result = JOptionPane.showConfirmDialog(this,
			CurrentLocale.get("com.tle.admin.schema.manager.transformationstab.delete")); //$NON-NLS-1$
		if( result != JOptionPane.YES_OPTION )
		{
			return;
		}

		final int index = table.getSelectedRow();
		final SchemaTransform transform = model.get(index);

		GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct()
			{
				deleteXSLT(transform.getFilename());
				return null;
			}

			@Override
			public void finished()
			{
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
		worker.setComponent(this);
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

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		updateButtons();
	}

	public void addToChangeListener(ChangeDetector changeDetector)
	{
		changeDetector.watch(model);
	}

	public void load()
	{
		Schema entity = state.getEntity();
		model.load(handleImports ? entity.getImportTransforms() : entity.getExportTransforms());
	}

	public void save()
	{
		List<SchemaTransform> transforms = model.save();
		Schema entity = state.getEntity();
		if( handleImports )
		{
			entity.setImportTransforms(transforms);
		}
		else
		{
			entity.setExportTransforms(transforms);
		}
	}

	private static class MyTableModel extends AbstractTableModel
	{
		private String[] names;

		private List<SchemaTransform> transforms;

		public MyTableModel()
		{
			transforms = new ArrayList<SchemaTransform>();
		}

		public void load(List<SchemaTransform> transforms)
		{
			this.transforms = new ArrayList<SchemaTransform>();
			if( !Check.isEmpty(transforms) )
			{
				this.transforms.addAll(transforms);
			}
			dataHasChanged();
		}

		public List<SchemaTransform> save()
		{
			return new ArrayList<SchemaTransform>(transforms);
		}

		public void add(SchemaTransform transform)
		{
			transforms.add(transform);
			dataHasChanged();
		}

		public SchemaTransform get(int row)
		{
			return transforms.get(row);
		}

		public void remove(int row)
		{
			transforms.remove(row);
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
			return transforms.size();
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			SchemaTransform transform = transforms.get(rowIndex);
			if( columnIndex == 0 )
			{
				return transform.getType();
			}
			else
			{
				return transform.getFilename();
			}
		}

		@Override
		public String getColumnName(int column)
		{
			return getNames()[column];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			return String.class;
		}

		private String[] getNames()
		{
			if( names == null )
			{
				names = new String[]{
						CurrentLocale.get("com.tle.admin.schema.manager.transformationstab.column.schema"),
						CurrentLocale.get("com.tle.admin.schema.manager.transformationstab.column.template")};
			}
			return names;
		}
	}
}
