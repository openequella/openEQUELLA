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

package com.tle.common.accesscontrolbuilder;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.gui.common.actions.AddAction;
import com.tle.admin.gui.common.actions.DownAction;
import com.tle.admin.gui.common.actions.JTextlessButton;
import com.tle.admin.gui.common.actions.RemoveAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.gui.common.actions.UpAction;
import com.tle.beans.security.ACLEntryMapping;
import com.tle.client.gui.popup.TablePopupListener;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.ExpressionTableCellRenderer;
import com.tle.common.recipientselector.WhoTableCellEditor;
import com.tle.common.security.PrivilegeTree;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.common.security.remoting.RemoteTLEAclManager;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class AdvancedEditorPanel extends JComponent implements ListSelectionListener, ActionListener
{
	private final String privilege;
	private final Object domainObj;

	private final List<TLEAction> actions;

	private JTable privilegeTable;
	private MyTableModel privilegeModel;

	private TableLayout layout;
	private JCheckBox showOverrides;
	private JCheckBox showDefaults;
	private OverrideDefaultAclViewer overridesViewer;
	private OverrideDefaultAclViewer defaultsViewer;
	private OverrideDefaultAclViewer.Filter overridesFilter;
	private OverrideDefaultAclViewer.Filter defaultsFilter;
	private final RemoteTLEAclManager aclManager;
	private final RemoteUserService userService;

	public AdvancedEditorPanel(RemoteTLEAclManager aclManager, RemoteUserService userService, Node privNode,
		PrivilegeList accessModel, Object domainObj)
	{
		this.aclManager = aclManager;
		this.userService = userService;
		this.privilege = accessModel.getPrivilege();
		this.domainObj = domainObj;

		actions = new ArrayList<TLEAction>();
		actions.add(addAction);
		actions.add(removeAction);
		actions.add(upAction);
		actions.add(downAction);

		setupGUI(privNode, accessModel);

		setupFilters(privNode);
	}

	@SuppressWarnings("nls")
	private void setupGUI(Node privNode, PrivilegeList accessModel)
	{
		JButton add = new JButton(addAction);
		JButton remove = new JButton(removeAction);
		JButton up = new JTextlessButton(upAction);
		JButton down = new JTextlessButton(downAction);

		privilegeModel = new MyTableModel(privNode, accessModel);
		privilegeTable = new JTable(privilegeModel);
		privilegeTable.addMouseListener(new TablePopupListener(privilegeTable, actions));

		ListSelectionModel selectionModel = privilegeTable.getSelectionModel();
		selectionModel.addListSelectionListener(this);
		selectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		TableColumn actionColumn = privilegeTable.getColumnModel().getColumn(0);
		actionColumn.setCellRenderer(new ActionTableCellRenderer());
		actionColumn.setCellEditor(new ActionTableCellEditor());

		TableColumn whoColumn = privilegeTable.getColumnModel().getColumn(1);
		whoColumn.setCellRenderer(new ExpressionTableCellRenderer(userService));
		whoColumn.setCellEditor(new WhoTableCellEditor(userService, this));

		JScrollPane scroller = new JScrollPane(privilegeTable);

		final int height1 = add.getPreferredSize().height;
		final int width1 = remove.getPreferredSize().width;
		final int[] rows = {height1, height1, height1, height1, TableLayout.FILL,};
		final int[] cols = {TableLayout.FILL, width1,};

		JPanel all = new JPanel(new TableLayout(rows, cols));
		all.add(scroller, new Rectangle(0, 0, 1, 5));
		all.add(add, new Rectangle(1, 0, 1, 1));
		all.add(remove, new Rectangle(1, 1, 1, 1));
		all.add(up, new Rectangle(1, 2, 1, 1));
		all.add(down, new Rectangle(1, 3, 1, 1));

		showOverrides = new JCheckBox(
			CurrentLocale.get("com.tle.admin.security.editors.advancededitorpanel.overriding"));
		showDefaults = new JCheckBox(CurrentLocale.get("com.tle.admin.security.editors.advancededitorpanel.default"));

		showOverrides.addActionListener(this);
		showDefaults.addActionListener(this);

		final int mainHeight1 = showOverrides.getPreferredSize().height;
		final int[] mainRows = {TableLayout.INVISIBLE, TableLayout.FILL, TableLayout.INVISIBLE, mainHeight1,};
		final int[] mainCols = {TableLayout.FILL, TableLayout.FILL, width1,};
		layout = new TableLayout(mainRows, mainCols);
		setLayout(layout);

		add(new JLabel(CurrentLocale.get("com.tle.admin.security.editors.advancededitorpanel.overrides")),
			new Rectangle(2, 0, 1, 1));
		add(all, new Rectangle(0, 1, 3, 1));
		add(new JLabel(CurrentLocale.get("com.tle.admin.security.editors.advancededitorpanel.defaults")),
			new Rectangle(2, 2, 1, 1));
		add(showOverrides, new Rectangle(0, 3, 1, 1));
		add(showDefaults, new Rectangle(1, 3, 1, 1));

		updateButtons();
	}

	private void setupFilters(Node privNode)
	{
		final int priority = PrivilegeTree.isOverrideDefault(privNode, privilege) ? privNode.getOverridePriority()
			: SecurityConstants.PRIORITY_OBJECT_INSTANCE;

		overridesFilter = new OverrideDefaultAclViewer.Filter()
		{
			@Override
			public boolean filter(ACLEntryMapping entry)
			{
				return entry.getPriority() > priority;
			}
		};

		defaultsFilter = new OverrideDefaultAclViewer.Filter()
		{
			@Override
			public boolean filter(ACLEntryMapping entry)
			{
				return entry.getPriority() < -priority;
			}
		};
	}

	private void updateButtons()
	{
		for( TLEAction action : actions )
		{
			action.update();
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
		if( e.getSource() == showOverrides )
		{
			new ViewerChanger(overridesViewer, 0, overridesFilter)
			{
				@Override
				public void setOldViewer(OverrideDefaultAclViewer odav)
				{
					overridesViewer = odav;
				}
			}.start();
		}
		else if( e.getSource() == showDefaults )
		{
			new ViewerChanger(defaultsViewer, 2, defaultsFilter)
			{
				@Override
				public void setOldViewer(OverrideDefaultAclViewer odav)
				{
					defaultsViewer = odav;
				}
			}.start();
		}
	}

	private final TLEAction addAction = new AddAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			privilegeTable.editingCanceled(null);
			privilegeModel.addEntry(privilegeTable.getSelectedRow());
		}
	};

	private final TLEAction removeAction = new RemoveAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			privilegeTable.editingCanceled(null);
			int[] indices = privilegeTable.getSelectedRows();
			for( int i = indices.length - 1; i >= 0; i-- )
			{
				privilegeModel.removeEntry(indices[i]);
			}
		}

		@Override
		public void update()
		{
			setEnabled(privilegeTable.getSelectedRowCount() > 0);
		}
	};

	private final TLEAction upAction = new UpAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			privilegeTable.editingCanceled(null);
			int row = privilegeTable.getSelectedRow();
			privilegeModel.swapRows(row - 1, row);
			privilegeTable.getSelectionModel().setSelectionInterval(row - 1, row - 1);
		}

		@Override
		public void update()
		{
			int[] rows = privilegeTable.getSelectedRows();
			setEnabled(rows.length == 1 && rows[0] > 0);
		}
	};

	private final TLEAction downAction = new DownAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			privilegeTable.editingCanceled(null);
			int row = privilegeTable.getSelectedRow();
			privilegeModel.swapRows(row, row + 1);
			privilegeTable.getSelectionModel().setSelectionInterval(row + 1, row + 1);
		}

		@Override
		public void update()
		{
			int[] rows = privilegeTable.getSelectedRows();
			setEnabled(rows.length == 1 && rows[0] < privilegeTable.getRowCount() - 1);
		}
	};

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

	/**
	 * @author Nicholas Read
	 */
	private static class MyTableModel extends AbstractTableModel
	{
		private static final String FIRST = CurrentLocale.get("security.editor.advanced.columnname.action"); //$NON-NLS-1$
		private static final String SECOND = CurrentLocale.get("security.editor.advanced.columnname.who"); //$NON-NLS-1$
		private static final String THIRD = CurrentLocale.get("security.editor.advanced.columnname.override"); //$NON-NLS-1$

		private final PrivilegeList model;
		private final Node privNode;

		public MyTableModel(Node privNode, PrivilegeList accessModel)
		{
			this.privNode = privNode;
			this.model = accessModel;
		}

		private List<PrivilegeListEntry> getEntries()
		{
			List<PrivilegeListEntry> entries = model.getEntries();
			if( entries == null )
			{
				entries = new ArrayList<PrivilegeListEntry>();
				model.setEntries(entries);
			}
			return entries;
		}

		public void addEntry(int basedOnRow)
		{
			PrivilegeListEntry entry = new PrivilegeListEntry();
			if( basedOnRow < 0 )
			{
				entry.setGranted(true);
				entry.setOverride(false);
				entry.setWho(SecurityConstants.getRecipient(Recipient.EVERYONE));
			}
			else
			{
				PrivilegeListEntry srcEntry = getEntries().get(basedOnRow);
				entry.setGranted(srcEntry.isGranted());
				entry.setOverride(srcEntry.isOverride());
				entry.setWho(srcEntry.getWho());
			}

			getEntries().add(entry);
			int lastRow = getRowCount() - 1;
			fireTableRowsInserted(lastRow, lastRow);
		}

		public void removeEntry(int index)
		{
			getEntries().remove(index);
			fireTableRowsDeleted(index, index);
		}

		public void swapRows(int row1, int row2)
		{
			Collections.swap(getEntries(), row1, row2);
			fireTableRowsUpdated(row1, row2);
		}

		@Override
		public String getColumnName(int column)
		{
			switch( column )
			{
				case 0:
					return FIRST;
				case 1:
					return SECOND;
				case 2:
					return THIRD;
				default:
					throw new RuntimeException();
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			if( columnIndex == 2 )
			{
				return Boolean.class;
			}
			return super.getColumnClass(columnIndex);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return true;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			PrivilegeListEntry entry = getEntries().get(rowIndex);
			switch( columnIndex )
			{
				case 0:
					entry.setGranted((Boolean) aValue);
					break;
				case 1:
					entry.setWho((String) aValue);
					break;
				case 2:
					entry.setOverride((Boolean) aValue);
					break;
				default:
					throw new RuntimeException();
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount()
		{
			return PrivilegeTree.isOverrideDefault(privNode, model.getPrivilege()) ? 3 : 2;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount()
		{
			return getEntries().size();
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			PrivilegeListEntry entry = getEntries().get(rowIndex);
			switch( columnIndex )
			{
				case 0:
					return entry.isGranted();
				case 1:
					return entry.getWho();
				case 2:
					return entry.isOverride();
				default:
					throw new RuntimeException();
			}
		}
	}

	/**
	 * @author Nicholas Read
	 */
	private abstract class ViewerChanger extends GlassSwingWorker<OverrideDefaultAclViewer>
	{
		private final int row;
		private final OverrideDefaultAclViewer.Filter filter;

		private final OverrideDefaultAclViewer oldViewer;

		public abstract void setOldViewer(OverrideDefaultAclViewer viewer);

		public ViewerChanger(OverrideDefaultAclViewer viewer, int row, OverrideDefaultAclViewer.Filter filter)
		{
			this.oldViewer = viewer;
			this.row = row;
			this.filter = filter;

			setComponent(AdvancedEditorPanel.this);
		}

		@Override
		public OverrideDefaultAclViewer construct() throws Exception
		{
			if( oldViewer == null )
			{
				return new OverrideDefaultAclViewer(aclManager, userService, domainObj, privilege, filter);
			}
			return null;
		}

		@Override
		public void finished()
		{
			if( get() != null )
			{
				layout.setRowSize(row, TableLayout.FILL);
				add(get(), new Rectangle(0, row, 2, 1));
			}
			else
			{
				layout.setRowSize(row, TableLayout.INVISIBLE);
				remove(oldViewer);
			}
			setOldViewer(get());

			AdvancedEditorPanel.this.invalidate();
			AdvancedEditorPanel.this.revalidate();
			AdvancedEditorPanel.this.updateUI();
			AdvancedEditorPanel.this.repaint();
		}
	}
}
