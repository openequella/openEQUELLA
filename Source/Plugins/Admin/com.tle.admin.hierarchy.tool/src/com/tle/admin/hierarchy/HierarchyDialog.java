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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ComponentHelper;
import com.tle.admin.common.gui.tree.AbstractTreeEditor;
import com.tle.admin.common.gui.tree.AbstractTreeEditorTree;
import com.tle.admin.common.gui.tree.AbstractTreeNodeEditor;
import com.tle.admin.common.gui.tree.BasicMessageEditor;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.beans.hierarchy.HierarchyPack;
import com.tle.beans.hierarchy.HierarchyTreeNode;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.client.EntityCache;
import com.tle.common.hierarchy.RemoteHierarchyService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.remoting.RemoteTLEAclManager;

@SuppressWarnings("nls")
public class HierarchyDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	protected AbstractTreeEditor<HierarchyTreeNode> tree;

	public HierarchyDialog(Frame frame, final ClientService clientService)
	{
		super(frame);

		final RemoteHierarchyService hierarchyService = clientService.getService(RemoteHierarchyService.class);

		tree = new AbstractTreeEditor<HierarchyTreeNode>()
		{
			private static final long serialVersionUID = 1L;

			protected EntityCache cache;

			@Override
			protected AbstractTreeNodeEditor createEditor(HierarchyTreeNode node)
			{
				if( !tree.canEdit(node) )
				{
					return new BasicMessageEditor(CurrentLocale.get("com.tle.admin.hierarchy.tool.notopic.noteditable"));
				}

				HierarchyPack pack = hierarchyService.getHierarchyPack(node.getId());
				if( cache == null )
				{
					cache = new EntityCache(clientService);
				}
				return new TopicEditor(clientService, cache, node, pack);
			}

			@Override
			protected AbstractTreeEditorTree<HierarchyTreeNode> createTree()
			{
				Collection<String> privs = Collections.singleton("EDIT_HIERARCHY_TOPIC");
				final boolean canAddRootTopics = !clientService.getService(RemoteTLEAclManager.class)
					.filterNonGrantedPrivileges(null, privs).isEmpty();

				return new TreeEditor(hierarchyService, canAddRootTopics);
			}
		};

		JPanel all = new JPanel(new MigLayout("fill, wrap 1", "[grow]", "[fill, grow][][]"));
		all.add(tree, "grow, push");
		all.add(new JSeparator(), "growx");
		all.add(new JButton(closeAction), "alignx right");

		setTitle(CurrentLocale.get("com.tle.admin.hierarchy.tool.hierarchydialog.title"));
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		getContentPane().add(all);
		setModal(true);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				closeAction.actionPerformed(null);
			}
		});

		ComponentHelper.percentageOfScreen(this, 0.9f, 0.9f);
		ComponentHelper.centreOnScreen(this);
	}

	private final TLEAction closeAction = new com.tle.admin.gui.common.actions.CloseAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			Runnable actionAfterSave = new Runnable()
			{
				@Override
				public void run()
				{
					dispose();
				}
			};
			tree.saveChanges(actionAfterSave);
		}
	};
}
