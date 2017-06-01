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

package com.tle.admin.security.tree.model;

import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.dytech.gui.ChangeDetector;
import com.google.common.collect.Lists;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;

@SuppressWarnings("nls")
public abstract class AbstractAclEditor extends JPanel
{
	private static final long serialVersionUID = 1L;

	protected List<TLEAction> actions;
	protected final ChangeDetector changeDetector = new ChangeDetector();
	protected JTable table;
	protected boolean allowEditing;

	protected final Node privilegeNode;
	protected final Object target;

	protected TargetList targetList;

	public AbstractAclEditor(Node privilegeNode, Object target, boolean allowEditing)
	{
		this.privilegeNode = privilegeNode;
		this.target = target;
		this.allowEditing = allowEditing;
	}

	protected void load(TargetList targetList)
	{
		this.targetList = targetList;
	}

	protected abstract class AbstractAclEditorTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;
		protected final String action = CurrentLocale.get("security.editor.advanced.columnname.action");
		protected final String privilege = CurrentLocale.get("security.editor.advanced.columnname.privilege");
		protected final String who = CurrentLocale.get("security.editor.advanced.columnname.who");
		protected final String override = CurrentLocale.get("security.editor.advanced.columnname.override");

		private TargetList targetList;
		protected Node privilegeNode;
		protected String defaultPrivilege;

		public AbstractAclEditorTableModel(Node privilegeNode, TargetList targetList, String defaultPrivilege)
		{
			this.privilegeNode = privilegeNode;
			this.targetList = targetList;
			this.defaultPrivilege = defaultPrivilege;
		}

		public void addEntry(int basedOnRow)
		{
			TargetListEntry entry = new TargetListEntry();
			if( basedOnRow < 0 )
			{
				entry.setGranted(true);
				entry.setOverride(false);
				entry.setPrivilege(defaultPrivilege);
				entry.setWho(SecurityConstants.getRecipient(Recipient.EVERYONE));
			}
			else
			{
				TargetListEntry basedOn = getTargetList().getEntries().get(basedOnRow);
				entry.setGranted(basedOn.isGranted());
				entry.setOverride(basedOn.isOverride());
				entry.setPrivilege(basedOn.getPrivilege());
				entry.setWho(basedOn.getWho());
			}

			getTargetList().getEntries().add(entry);
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
			Collections.swap(getTargetList().getEntries(), row1, row2);
			fireTableRowsUpdated(row1, row2);
		}

		@Override
		public int getRowCount()
		{
			return getEntries().size();
		}

		private List<TargetListEntry> getEntries()
		{
			List<TargetListEntry> entries = targetList.getEntries();
			if( entries == null )
			{
				entries = Lists.newArrayList();
				targetList.setEntries(entries);
			}
			return entries;
		}

		public TargetList getTargetList()
		{
			return targetList;
		}

		public void setTargetList(TargetList targetList)
		{
			this.targetList = targetList;
		}
	}
}
