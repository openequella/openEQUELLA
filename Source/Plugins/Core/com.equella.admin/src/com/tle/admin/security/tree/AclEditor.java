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

package com.tle.admin.security.tree;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.gui.common.actions.AddAction;
import com.tle.admin.gui.common.actions.DownAction;
import com.tle.admin.gui.common.actions.JTextlessButton;
import com.tle.admin.gui.common.actions.RemoveAction;
import com.tle.admin.gui.common.actions.SaveAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.gui.common.actions.UpAction;
import com.tle.admin.security.tree.model.AbstractAclEditor;
import com.tle.client.gui.popup.TablePopupListener;
import com.tle.common.accesscontrolbuilder.ActionTableCellEditor;
import com.tle.common.accesscontrolbuilder.ActionTableCellRenderer;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.ExpressionTableCellRenderer;
import com.tle.common.recipientselector.WhoTableCellEditor;
import com.tle.common.security.PrivilegeTree;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.common.security.remoting.RemoteTLEAclManager;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class AclEditor extends AbstractAclEditor implements SecurityTreeTab
{
	protected ClientService services;
	private MyTableModel model;

	public AclEditor(ClientService services, Node privilegeNode, Object target, boolean allowEditing)
	{
		super(privilegeNode, target, allowEditing);
		this.services = services;
		load(services.getService(RemoteTLEAclManager.class).getTargetList(privilegeNode, target));

		setupGui();

		changeDetector.watch(model);
	}


	private void setupGui()
	{
		actions = new ArrayList<TLEAction>();
		actions.add(addAction);
		actions.add(removeAction);
		actions.add(upAction);
		actions.add(downAction);

		Set<String> privileges = PrivilegeTree.getAllPrivilegesForNode(privilegeNode).keySet();
		JComboBox<String> privSelector = new JComboBox<String>(privileges.toArray(new String[0]));

		JButton add = new JButton(addAction);
		JButton remove = new JButton(removeAction);
		JButton up = new JTextlessButton(upAction);
		JButton down = new JTextlessButton(downAction);
		JButton save = new JButton(saveAction);

		model = new MyTableModel(privilegeNode, targetList, privSelector.getItemAt(0));

		table = new JTable(model);
		table.addMouseListener(new TablePopupListener(table, actions));
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				updateButtons();
			}
		});

		TableColumn actionColumn = table.getColumnModel().getColumn(0);
		actionColumn.setCellRenderer(new ActionTableCellRenderer());
		actionColumn.setCellEditor(new ActionTableCellEditor());

		table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(privSelector));

		TableColumn whoColumn = table.getColumnModel().getColumn(2);
		whoColumn.setCellRenderer(new ExpressionTableCellRenderer(services.getService(RemoteUserService.class)));
		whoColumn.setCellEditor(new WhoTableCellEditor(services.getService(RemoteUserService.class), this));

		table.getColumnModel().getColumn(3).setCellRenderer(new OverrideRenderer());

		JScrollPane scroller = new JScrollPane(table);

		setBorder(AppletGuiUtils.DEFAULT_BORDER);
		setLayout(new MigLayout("wrap", "[grow, fill][fill]"));

		add(scroller, "spany 5, growy");
		add(add);
		add(remove, "skip 1");
		add(up, "skip 1");
		add(down, "skip 1");
		add(save, "pushy, skip 1, bottom");

		updateButtons();
	}

	private void updateButtons()
	{
		for( TLEAction action : actions )
		{
			action.update();
		}
		saveAction.update();
	}

	@Override
	public boolean hasChanges()
	{
		table.editingStopped(null);
		return changeDetector.hasDetectedChanges();
	}

	@Override
	public void saveChanges()
	{
		table.editingStopped(null);

		services.getService(RemoteTLEAclManager.class)
			.setTargetListAndReindex(privilegeNode.name(), target, targetList);

		changeDetector.clearChanges();
	}

	private final TLEAction addAction = new AddAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			table.editingCanceled(null);
			model.addEntry(table.getSelectedRow());

			int i = model.getRowCount() - 1;
			table.getSelectionModel().setSelectionInterval(i, i);
		}

		@Override
		public void update()
		{
			setEnabled(allowEditing);
		}
	};

	private final TLEAction removeAction = new RemoveAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			table.editingCanceled(null);
			int[] indices = table.getSelectedRows();
			for( int i = indices.length - 1; i >= 0; i-- )
			{
				model.removeEntry(indices[i]);
			}
		}

		@Override
		public void update()
		{
			setEnabled(allowEditing && table.getSelectedRowCount() > 0);
		}
	};

	private final TLEAction upAction = new UpAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			table.editingCanceled(null);
			int row = table.getSelectedRow();
			model.swapRows(row - 1, row);
			table.getSelectionModel().setSelectionInterval(row - 1, row - 1);
		}

		@Override
		public void update()
		{
			int[] rows = table.getSelectedRows();
			setEnabled(allowEditing && rows.length == 1 && rows[0] > 0);
		}
	};

	private final TLEAction downAction = new DownAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			table.editingCanceled(null);
			int row = table.getSelectedRow();
			model.swapRows(row, row + 1);
			table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
		}

		@Override
		public void update()
		{
			int[] rows = table.getSelectedRows();
			setEnabled(allowEditing && rows.length == 1 && rows[0] < table.getRowCount() - 1);
		}
	};

	private final TLEAction saveAction = new SaveAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if( hasChanges() )
			{
				GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
				{
					@Override
					public Object construct() throws Exception
					{
						saveChanges();
						return null;
					}

					@Override
					public void finished()
					{
						JOptionPane.showMessageDialog(getComponent(),
							CurrentLocale.get("com.tle.admin.security.tree.acleditor.saved"));
					}

					@Override
					public void exception()
					{
						JOptionPane.showMessageDialog(getComponent(),
							CurrentLocale.get("com.tle.admin.security.tree.acleditor.error"));
						getException().printStackTrace();
					}
				};
				worker.setComponent(AclEditor.this);
				worker.start();
			}
		}

		@Override
		public void update()
		{
			setEnabled(allowEditing);
		}
	};

	/**
	 * @author Nicholas Read
	 */
	private class MyTableModel extends AbstractAclEditorTableModel
	{
		public MyTableModel(Node privilegeNode, TargetList targetList, String defaultPrivilege)
		{
			super(privilegeNode, targetList, defaultPrivilege);
		}

		@Override
		public int getColumnCount()
		{
			return 4;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			switch( columnIndex )
			{
				case 0:
				case 3:
					return Boolean.class;
				default:
					return super.getColumnClass(columnIndex);
			}
		}

		@Override
		public String getColumnName(int column)
		{
			switch( column )
			{
				case 0:
					return action;
				case 1:
					return privilege;
				case 2:
					return who;
				case 3:
					return override;
				default:
					throw new IllegalStateException("We should never reach here");
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			TargetListEntry entry = getTargetList().getEntries().get(rowIndex);
			switch( columnIndex )
			{
				case 0:
					return entry.isGranted();
				case 1:
					return entry.getPrivilege();
				case 2:
					return entry.getWho();
				case 3:
					return allowOverrideDefault(entry) ? entry.isOverride() : false;
				default:
					throw new IllegalStateException("We should never reach here");
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			TargetListEntry entry = getTargetList().getEntries().get(rowIndex);
			switch( columnIndex )
			{
				case 0:
					entry.setGranted((Boolean) aValue);
					break;
				case 1:
					entry.setPrivilege((String) aValue);
					break;
				case 2:
					entry.setWho((String) aValue);
					break;
				case 3:
					entry.setOverride((Boolean) aValue);
					break;
				default:
					throw new IllegalStateException("We should never reach here");
			}
			changeDetector.forceChange(this);

			fireTableRowsUpdated(rowIndex, rowIndex);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			switch( columnIndex )
			{
				case 0:
				case 1:
				case 2:
					return true;
				case 3:
					TargetListEntry entry = getTargetList().getEntries().get(rowIndex);
					return allowOverrideDefault(entry);
				default:
					throw new IllegalStateException("We should never reach here");
			}
		}

		private boolean allowOverrideDefault(TargetListEntry entry)
		{
			return PrivilegeTree.isOverrideDefault(privilegeNode, entry.getPrivilege());
		}
	}
}
