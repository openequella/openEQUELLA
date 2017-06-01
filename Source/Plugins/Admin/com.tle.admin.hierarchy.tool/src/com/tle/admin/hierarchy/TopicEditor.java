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

package com.tle.admin.hierarchy;

import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.TableLayout;
import com.tle.admin.Driver;
import com.tle.admin.common.gui.tree.AbstractTreeNodeEditor;
import com.tle.admin.gui.EditorException;
import com.tle.beans.hierarchy.HierarchyPack;
import com.tle.beans.hierarchy.HierarchyTreeNode;
import com.tle.common.LazyTreeNode;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.client.EntityCache;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.hierarchy.RemoteHierarchyService;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class TopicEditor extends AbstractTreeNodeEditor
{
	private static final long serialVersionUID = 1L;

	private final RemoteHierarchyService hierarchyService;
	private final HierarchyPack pack;
	private final HierarchyTreeNode treeNode;

	private JTabbedPane tabs;

	public TopicEditor(ClientService clientService, EntityCache cache, HierarchyTreeNode treeNode, HierarchyPack pack)
	{
		this.hierarchyService = clientService.getService(RemoteHierarchyService.class);
		this.treeNode = treeNode;
		this.pack = pack;

		setup(clientService, cache);

		// Load the tabs
		apply(new Functor()
		{
			@Override
			public void apply(AbstractTopicEditorTab tab)
			{
				tab.load(TopicEditor.this.pack);
			}
		});
		changeDetector.clearChanges();
	}

	@SuppressWarnings("nls")
	private void setup(ClientService clientService, EntityCache cache)
	{
		tabs = new JTabbedPane();

		tabs.addTab(CurrentLocale.get("com.tle.admin.hierarchy.tool.topiceditor.details"), new DetailsTab(cache));
		tabs.addTab(CurrentLocale.get("com.tle.admin.hierarchy.tool.topiceditor.filtering"), new FilteringTab(cache,
			clientService));
		if( pack.getTopic().getParent() != null )
		{
			tabs.addTab(CurrentLocale.get("com.tle.admin.hierarchy.tool.topiceditor.inheritance"), new InheritanceTab(
				cache, clientService));
		}
		tabs.addTab(CurrentLocale.get("com.tle.admin.hierarchy.tool.topiceditor.virtual"), new VirtualisationTab(Driver
			.instance().getPluginService(), clientService));
		tabs.addTab(CurrentLocale.get("com.tle.admin.hierarchy.tool.topiceditor.resources"), new KeyResourcesTab(
			clientService));
		tabs.addTab(CurrentLocale.get("com.tle.admin.hierarchy.tool.topiceditor.control"), new AccessControlTab(
			clientService));

		JButton save = new JButton(createSaveAction());

		final int width1 = save.getPreferredSize().width;
		final int height1 = save.getPreferredSize().height;

		final int[] rows = {TableLayout.FILL, height1,};
		final int[] cols = {TableLayout.FILL, width1,};

		setLayout(new TableLayout(rows, cols));
		add(tabs, new Rectangle(0, 0, 2, 1));
		add(save, new Rectangle(1, 1, 1, 1));

		apply(new Functor()
		{
			@Override
			public void apply(AbstractTopicEditorTab tab)
			{
				tab.setup(changeDetector);
			}
		});
	}

	@Override
	protected LazyTreeNode getUpdatedNode()
	{
		treeNode.setName(CurrentLocale.get(pack.getTopic().getName()));
		return treeNode;
	}

	@Override
	public void save()
	{
		// Clear this as we don't want to send it back.
		pack.setInheritedItemDefinitions(null);

		apply(new Functor()
		{
			@Override
			public void apply(AbstractTopicEditorTab tab)
			{
				tab.save(pack);
			}
		});

		hierarchyService.edit(pack);
	}

	@Override
	protected void validation() throws EditorException
	{
		final int count = tabs.getTabCount();
		for( int i = 0; i < count; i++ )
		{
			AbstractTopicEditorTab tab = (AbstractTopicEditorTab) tabs.getComponentAt(i);
			tab.validation();
		}
	}

	private void apply(Functor functor)
	{
		final int count = tabs.getTabCount();
		for( int i = 0; i < count; i++ )
		{
			AbstractTopicEditorTab tab = (AbstractTopicEditorTab) tabs.getComponentAt(i);
			functor.apply(tab);
		}
	}

	private interface Functor
	{
		void apply(AbstractTopicEditorTab tab);
	}

	/**
	 * @author Nicholas Read
	 */
	public abstract static class AbstractTopicEditorTab extends JPanel
	{
		private static final long serialVersionUID = 1L;

		public AbstractTopicEditorTab()
		{
			setBorder(AppletGuiUtils.DEFAULT_BORDER);
		}

		public abstract void setup(ChangeDetector changeDetector);

		public abstract void load(HierarchyPack pack);

		public abstract void save(HierarchyPack pack);

		public abstract void validation() throws EditorException;
	}

}
