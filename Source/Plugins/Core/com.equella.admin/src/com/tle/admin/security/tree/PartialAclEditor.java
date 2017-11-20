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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.Changeable;
import com.tle.admin.gui.common.actions.AddAction;
import com.tle.admin.gui.common.actions.DownAction;
import com.tle.admin.gui.common.actions.JTextlessButton;
import com.tle.admin.gui.common.actions.RemoveAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.gui.common.actions.UpAction;
import com.tle.admin.security.tree.model.AbstractAclEditor;
import com.tle.client.gui.popup.TablePopupListener;
import com.tle.common.accesscontrolbuilder.ActionTableCellEditor;
import com.tle.common.accesscontrolbuilder.ActionTableCellRenderer;
import com.tle.common.security.PrivilegeTree;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;

@SuppressWarnings("nls")
public class PartialAclEditor extends AbstractAclEditor implements Changeable
{
	private PartialAclEditorTableModel model;

	public PartialAclEditor(Node privilegeNode)
	{
		super(privilegeNode, null, true);
		setupGui();
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

		model = new PartialAclEditorTableModel(privilegeNode, privSelector.getItemAt(0));

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

		JScrollPane scroller = new JScrollPane(table);

		setBorder(null);
		setLayout(new MigLayout("wrap, insets 0", "[grow, fill, push][fill]"));

		add(scroller, "growy, spany 4");
		add(add);
		add(remove, "skip 1");
		add(up, "skip 1");
		add(down, "pushy, skip 1, top");

		updateButtons();
		changeDetector.watch(model);
	}

	private void updateButtons()
	{
		for( TLEAction action : actions )
		{
			action.update();
		}
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

	private class PartialAclEditorTableModel extends AbstractAclEditorTableModel
	{
		public PartialAclEditorTableModel(Node privilegeNode, String defaultPrivilege)
		{
			super(privilegeNode, null, defaultPrivilege);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			switch( columnIndex )
			{
				case 0:
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
					return true;
				default:
					throw new IllegalStateException("We should never reach here");
			}
		}

		@Override
		public int getColumnCount()
		{
			return 2;
		}
	}

	public TargetList save()
	{
		TargetList tl = new TargetList();
		tl.setEntries(targetList.getEntries());
		return tl;
	}

	@Override
	public void load(TargetList targetList)
	{
		TargetList tl = new TargetList();
		tl.setEntries(targetList.getEntries());
		super.load(tl);
		model.setTargetList(tl);
		updateButtons();
		table.clearSelection();
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges();
	}

	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
	}
}
