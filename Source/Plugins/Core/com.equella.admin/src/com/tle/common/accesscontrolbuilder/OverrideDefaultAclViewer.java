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

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.tle.beans.security.ACLEntryMapping;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.ExpressionTableCellRenderer;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.remoting.RemoteTLEAclManager;
import com.tle.core.remoting.RemoteUserService;

public class OverrideDefaultAclViewer extends JScrollPane
{
	private static final long serialVersionUID = 1L;

	public OverrideDefaultAclViewer(RemoteTLEAclManager aclManager, RemoteUserService userService, Object domainObj,
		String privilege)
	{
		this(aclManager, userService, domainObj, privilege, null);
	}

	public OverrideDefaultAclViewer(RemoteTLEAclManager aclManager, RemoteUserService userService, Object domainObj,
		String privilege, Filter filter)
	{
		List<ACLEntryMapping> acls = aclManager.getAllEntriesForObjectOtherThanTheObject(domainObj, privilege);
		if( filter != null )
		{
			for( Iterator<ACLEntryMapping> iter = acls.iterator(); iter.hasNext(); )
			{
				ACLEntryMapping entry = iter.next();
				if( !filter.filter(entry) )
				{
					iter.remove();
				}
			}
		}

		MyTableModel model = new MyTableModel(acls);
		JTable table = new JTable(model);

		table.getColumnModel().getColumn(0).setCellRenderer(new ActionTableCellRenderer());
		table.getColumnModel().getColumn(1).setCellRenderer(new ExpressionTableCellRenderer(userService));

		setViewportView(table);
	}

	public interface Filter extends Serializable
	{
		boolean filter(ACLEntryMapping entry);
	}

	private static class MyTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;
		private static final String FIRST = CurrentLocale.get("security.editor.advanced.columnname.action"); //$NON-NLS-1$
		private static final String SECOND = CurrentLocale.get("security.editor.advanced.columnname.who"); //$NON-NLS-1$

		private final List<ACLEntryMapping> acls;

		public MyTableModel(List<ACLEntryMapping> acls)
		{
			this.acls = acls;
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
				default:
					throw new IllegalStateException();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount()
		{
			return 2;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount()
		{
			return acls.size();
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			ACLEntryMapping entry = acls.get(rowIndex);
			switch( columnIndex )
			{
				case 0:
					return entry.getGrant() == SecurityConstants.GRANT;
				case 1:
					return entry.getExpression();
				default:
					throw new IllegalStateException();
			}
		}
	}
}