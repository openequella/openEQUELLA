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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.beans.security.ACLEntryMapping;
import com.tle.common.accesscontrolbuilder.ActionTableCellRenderer;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.ExpressionTableCellRenderer;
import com.tle.common.security.PrivilegeTree;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.remoting.RemoteTLEAclManager;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class AclViewer extends JPanel implements ActionListener, SecurityTreeTab
{
	private static final long serialVersionUID = 1L;
	private final Node privilegeNode;
	protected final Object target;
	protected final RemoteTLEAclManager aclManager;
	private final RemoteUserService userService;

	private JComboBox<String> privSelector;
	protected MyTableModel model;

	public AclViewer(ClientService clientService, Node privilegeNode, Object target)
	{
		this.privilegeNode = privilegeNode;
		this.target = target;

		aclManager = clientService.getService(RemoteTLEAclManager.class);
		userService = clientService.getService(RemoteUserService.class);

		setupGui(clientService);
	}

	private void setupGui(ClientService clientService)
	{
		JLabel label = new JLabel(CurrentLocale.get("security.editor.selectprivilege")); //$NON-NLS-1$

		privSelector = new JComboBox<>();
		for( String privilege : new TreeSet<String>(PrivilegeTree.getPrivilegesForNode(privilegeNode)) )
		{
			privSelector.addItem(privilege);
		}
		privSelector.setSelectedItem(null);
		privSelector.addActionListener(this);

		model = new MyTableModel(clientService);

		JTable table = new JTable(model);
		table.getColumnModel().getColumn(2).setCellRenderer(new ExpressionTableCellRenderer(userService));
		table.getColumnModel().getColumn(1).setCellRenderer(new ActionTableCellRenderer());

		JScrollPane scroller = new JScrollPane(table);

		final int width = label.getPreferredSize().width;
		final int height = privSelector.getPreferredSize().height;
		final int[] rows = {height, TableLayout.FILL,};
		final int[] cols = {width, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));
		setBorder(AppletGuiUtils.DEFAULT_BORDER);

		add(label, new Rectangle(0, 0, 1, 1));
		add(privSelector, new Rectangle(1, 0, 1, 1));
		add(scroller, new Rectangle(0, 1, 2, 1));
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.security.tree.SecurityTreeTab#hasChanges()
	 */
	@Override
	public boolean hasChanges()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.security.tree.SecurityTreeTab#saveChanges()
	 */
	@Override
	public void saveChanges()
	{
		// Nothing to do here
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == privSelector )
		{
			final String privilege = (String) privSelector.getSelectedItem();

			GlassSwingWorker<List<ACLEntryMapping>> worker = new GlassSwingWorker<List<ACLEntryMapping>>()
			{
				@Override
				public List<ACLEntryMapping> construct() throws Exception
				{
					return aclManager.getAllEntriesForObject(target, privilege);
				}

				@Override
				public void finished()
				{
					model.setEntries(get());
				}
			};
			worker.setComponent(this);
			worker.start();
		}
	}

	private static class MyTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;
		private static final String FIRST = CurrentLocale.get("security.editor.advanced.columnname.target"); //$NON-NLS-1$
		private static final String SECOND = CurrentLocale.get("security.editor.advanced.columnname.action"); //$NON-NLS-1$
		private static final String THIRD = CurrentLocale.get("security.editor.advanced.columnname.who"); //$NON-NLS-1$

		private List<ACLEntryMapping> entries;
		private final TargetToNameMapping mapping;

		public MyTableModel(ClientService services)
		{
			mapping = new TargetToNameMapping(services);
		}

		public void setEntries(List<ACLEntryMapping> entries)
		{
			this.entries = entries;
			if( entries != null )
			{
				mapping.addEntries(entries);
			}
			fireTableDataChanged();
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount()
		{
			return entries == null ? 0 : entries.size();
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount()
		{
			return 3;
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
					throw new IllegalStateException();
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			switch( columnIndex )
			{
				case 1:
					return Boolean.class;
				default:
					return super.getColumnClass(columnIndex);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			ACLEntryMapping entry = entries.get(rowIndex);
			switch( columnIndex )
			{
				case 0:
					return mapping.getName(entry);
				case 1:
					return entry.getGrant() == SecurityConstants.GRANT;
				case 2:
					return entry.getExpression();
				default:
					throw new IllegalStateException();
			}
		}
	}
}
