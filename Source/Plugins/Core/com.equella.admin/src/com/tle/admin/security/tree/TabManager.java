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

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.tle.admin.security.tree.model.ItemSearchNode;
import com.tle.admin.security.tree.model.SecurityTreeNode;
import com.tle.beans.item.Item;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class TabManager extends JPanel
{
	private static final long serialVersionUID = 1L;
	private final ClientService services;
	private final boolean allowEditing;

	private JTabbedPane tabs;
	private ItemSearcher searcher;

	private GridLayout layout;

	public TabManager(ClientService services, boolean allowEditing)
	{
		this.services = services;
		this.allowEditing = allowEditing;

		tabs = new JTabbedPane();
		layout = new GridLayout(1, 1);

		setLayout(layout);
		add(tabs);
	}

	private void setSearchPanelVisible(boolean b)
	{
		if( !b && searcher != null || b && searcher == null )
		{
			this.removeAll();

			layout.setRows(b ? 2 : 1);
			if( searcher == null )
			{
				searcher = new ItemSearcher(this, services);
				this.add(searcher);
			}
			else
			{
				searcher = null;
			}
			this.add(tabs);
		}
	}

	public void updateTabs(SecurityTreeNode node)
	{
		if( node == null )
		{
			tabs.removeAll();
			tabs.addTab(CurrentLocale.get("com.tle.admin.security.tree.tabmanager.instructions"), new MessagePanel(
				CurrentLocale.get("com.tle.admin.security.tree.tabmanager.select")));
			setSearchPanelVisible(false);
		}
		else if( node instanceof ItemSearchNode )
		{
			updateTabsForNoSelection();
		}
		else
		{
			updateTabs(node.getPrivilegeNode(), node.getTargetObject());
			setSearchPanelVisible(false);
		}
	}

	public void updateTabs(Item item)
	{
		if( item == null )
		{
			updateTabsForNoSelection();
		}
		else
		{
			updateTabs(Node.ITEM, item);
		}
	}

	private void updateTabsForNoSelection()
	{
		tabs.removeAll();
		tabs.addTab(CurrentLocale.get("com.tle.admin.security.tree.tabmanager.instructions"), new MessagePanel(
			CurrentLocale.get("com.tle.admin.security.tree.tabmanager.search")));
		setSearchPanelVisible(true);
	}

	private void updateTabs(Node privilegeNode, Object target)
	{
		tabs.removeAll();
		if( privilegeNode != null )
		{
			tabs.addTab(CurrentLocale.get("com.tle.admin.security.tree.tabmanager.view"), new AclEditor(services,
				privilegeNode, target, allowEditing));

			if( !privilegeNode.isVirtual() )
			{
				tabs.addTab(CurrentLocale.get("com.tle.admin.security.tree.tabmanager.all"), new AclViewer(services,
					privilegeNode, target));
			}
		}
		else
		{
			tabs.addTab("No Options",
				new MessagePanel(CurrentLocale.get("com.tle.admin.security.tree.tabmanager.noconfig")));
		}
	}

	public boolean hasDetectedChanges()
	{
		int count = tabs.getTabCount();
		for( int i = 0; i < count; i++ )
		{
			SecurityTreeTab tab = (SecurityTreeTab) tabs.getComponentAt(i);
			if( tab.hasChanges() )
			{
				return true;
			}
		}
		return false;
	}

	public void saveTabs()
	{
		int count = tabs.getTabCount();
		for( int i = 0; i < count; i++ )
		{
			SecurityTreeTab tab = (SecurityTreeTab) tabs.getComponentAt(i);
			tab.saveChanges();
		}
	}
}
