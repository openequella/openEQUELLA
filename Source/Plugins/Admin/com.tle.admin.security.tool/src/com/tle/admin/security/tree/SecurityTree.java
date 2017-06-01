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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.gui.common.actions.CloseAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.security.tree.model.SecurityTreeModel;
import com.tle.admin.security.tree.model.SecurityTreeNode;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.plugins.PluginService;

/**
 * @author Nicholas Read
 */
public class SecurityTree implements TreeSelectionListener
{
	private final boolean allowEditing;

	protected SecurityTreeNode currentSelection;

	protected JTree tree;
	protected SecurityTreeModel model;
	protected TabManager tabManager;
	private JPanel content;
	private JDialog dialog;

	public SecurityTree(ClientService clientService, PluginService pluginService, boolean allowEditing)
	{
		this.allowEditing = allowEditing;

		setupGui(clientService, pluginService);
	}

	private void setupGui(ClientService clientService, PluginService pluginService)
	{
		model = new SecurityTreeModel(clientService, pluginService);

		tree = new JTree(model);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setCellRenderer(new MyTreeCellRenderer());
		tree.addTreeSelectionListener(this);

		tabManager = new TabManager(clientService, allowEditing);

		JScrollPane scroller = new JScrollPane(tree);
		scroller.setMinimumSize(new Dimension(200, Integer.MAX_VALUE));

		JSplitPane split = AppletGuiUtils.createSplitPane();
		split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		split.setContinuousLayout(true);
		split.add(scroller, JSplitPane.LEFT);
		split.add(tabManager, JSplitPane.RIGHT);

		JButton closeButton = new JButton(closeAction);

		final int[] rows = {TableLayout.FILL, closeButton.getPreferredSize().height,};
		final int[] cols = {TableLayout.FILL, closeButton.getPreferredSize().width,};

		content = new JPanel(new TableLayout(rows, cols));
		content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		content.add(split, new Rectangle(0, 0, 2, 1));
		content.add(closeButton, new Rectangle(1, 1, 1, 1));

		updateEditor();
	}

	private void updateEditor()
	{
		final SecurityTreeNode newSelection = getSelectedNode();
		if( newSelection != null && newSelection.equals(currentSelection) )
		{
			return;
		}

		if( trySave(new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct() throws Exception
			{
				tabManager.saveTabs();
				return null;
			}

			@Override
			public void finished()
			{
				tree.setSelectionPath(model.getPathToRoot(newSelection));
				currentSelection = newSelection;
				tabManager.updateTabs(currentSelection);
			}
		}) == JOptionPane.NO_OPTION )
		{
			currentSelection = newSelection;
			tabManager.updateTabs(currentSelection);
		}
	}

	int trySave(GlassSwingWorker<?> worker)
	{
		int result = JOptionPane.NO_OPTION;
		if( allowEditing && currentSelection != null && tabManager.hasDetectedChanges() )
		{
			String title = CurrentLocale.get("security.tree.prompt.savechanges.title"); //$NON-NLS-1$
			String msg = CurrentLocale.get("security.tree.prompt.savechanges.body"); //$NON-NLS-1$
			Object[] buttons = {CurrentLocale.get("prompts.save"), //$NON-NLS-1$
					CurrentLocale.get("prompts.discard"), CurrentLocale.get("prompts.cancel"),}; //$NON-NLS-1$ //$NON-NLS-2$

			result = JOptionPane.showOptionDialog(dialog, msg, title, JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, buttons, buttons[2]);

			if( result != JOptionPane.NO_OPTION )
			{
				if( result == JOptionPane.YES_OPTION )
				{
					worker.setComponent(dialog);
					worker.start();
				}
				else
				{
					tree.setSelectionPath(model.getPathToRoot(currentSelection));
				}
			}
		}
		return result;
	}

	private SecurityTreeNode getSelectedNode()
	{
		TreePath selectionPath = tree.getSelectionPath();
		if( selectionPath == null )
		{
			return null;
		}
		else
		{
			return (SecurityTreeNode) selectionPath.getLastPathComponent();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event
	 * .TreeSelectionEvent)
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		updateEditor();
	}

	public void showDialog(Component parent)
	{
		dialog = ComponentHelper.createJDialog(parent);
		dialog.setTitle(CurrentLocale.get(
			"com.tle.admin.security.tree.securitytree.title", Driver.instance().getInstitutionName())); //$NON-NLS-1$
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.setModal(true);
		dialog.setContentPane(content);

		dialog.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				attemptToCloseWindow();
			}
		});

		ComponentHelper.percentageOfScreen(dialog, 0.9f, 0.9f);
		ComponentHelper.centreOnScreen(dialog);

		dialog.setVisible(true);
	}

	private final TLEAction closeAction = new CloseAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			attemptToCloseWindow();
		}
	};

	protected void attemptToCloseWindow()
	{
		boolean close = trySave(new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct() throws Exception
			{
				tabManager.saveTabs();
				return null;
			}
		}) != JOptionPane.CANCEL_OPTION;

		if( close )
		{
			dialog.setVisible(false);
			dialog.dispose();
		}
	}

	static class MyTreeCellRenderer extends DefaultTreeCellRenderer
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus1)
		{
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus1);

			SecurityTreeNode node = (SecurityTreeNode) value;
			setText(node.getDisplayName());

			return this;
		}
	}
}
