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

package com.tle.admin.common.gui.tree;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.common.LazyTreeNode;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;

@SuppressWarnings("nls")
public abstract class AbstractTreeEditor<NodeType extends LazyTreeNode> extends JSplitPane
	implements
		TreeSelectionListener
{
	protected final JPanel editArea;
	protected final AbstractTreeEditorTree<NodeType> tree;

	protected AbstractTreeNodeEditor currentEditor;
	protected BasicMessageEditor noSelectionEditor;

	protected abstract AbstractTreeEditorTree<NodeType> createTree();

	protected abstract AbstractTreeNodeEditor createEditor(NodeType node);

	public AbstractTreeEditor()
	{
		tree = createTree();
		tree.addTreeSelectionListener(this);
		tree.setPreferredSize(new Dimension(325, 0));
		tree.setMinimumSize(new Dimension(325, 0));

		// Ensure the root nodes start loading
		tree.loadChildren(tree.getRootNode());

		editArea = new JPanel(new GridLayout(1, 1));

		AppletGuiUtils.removeBordersFromSplitPane(this);
		setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		setContinuousLayout(true);
		setResizeWeight(0.05);

		add(tree, JSplitPane.LEFT);
		add(editArea, JSplitPane.RIGHT);

		noSelectionEditor = new BasicMessageEditor(
			CurrentLocale.get("com.tle.admin.gui.common.tree.nodeeditor.nonodeselected"));

		showTreeNodeEditor(noSelectionEditor);
	}

	/**
	 * Changes the editor pane to show the topic editor, or the blank editor,
	 * depending on the given value.
	 * 
	 * @param b true if the topic editor should be shown.
	 */
	protected void showTreeNodeEditor(AbstractTreeNodeEditor newEditor)
	{
		// Change
		if( currentEditor != null )
		{
			currentEditor.removeNodeChangeListener(tree);
		}

		currentEditor = newEditor;

		if( currentEditor != null )
		{
			currentEditor.addNodeChangeListener(tree);
		}

		editArea.removeAll();
		editArea.add(currentEditor);
		editArea.updateUI();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event
	 * .TreeSelectionEvent)
	 */
	@Override
	public void valueChanged(final TreeSelectionEvent e)
	{
		final TreePath newPath = e.getNewLeadSelectionPath();
		saveChanges(new Runnable()
		{
			@Override
			public void run()
			{
				GlassSwingWorker<?> worker = new GlassSwingWorker<AbstractTreeNodeEditor>()
				{
					@Override
					@SuppressWarnings("unchecked")
					public AbstractTreeNodeEditor construct()
					{
						if( newPath == null )
						{
							return noSelectionEditor;
						}
						else
						{
							return createEditor((NodeType) newPath.getLastPathComponent());
						}
					}

					@Override
					public void finished()
					{
						showTreeNodeEditor(get());
					}

					@Override
					public void exception()
					{
						getException().printStackTrace();
					}
				};
				worker.setComponent(AbstractTreeEditor.this);
				worker.start();
			}
		});
	}

	public void saveChanges(final Runnable actionAfterSave)
	{
		if( currentEditor == null || !currentEditor.hasChanges() )
		{
			actionAfterSave.run();
			return;
		}

		String message = CurrentLocale.get("com.tle.admin.gui.common.tree.nodeeditor.saveconfirm");
		String[] buttons = {CurrentLocale.get("com.dytech.edge.admin.gui.save"),
				CurrentLocale.get("com.dytech.edge.admin.gui.dontsave"),
				CurrentLocale.get("com.dytech.edge.admin.gui.cancel")};
		final int confirm = JOptionPane.showOptionDialog(this, message,
			CurrentLocale.get("com.dytech.edge.admin.gui.savechanges"), JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.WARNING_MESSAGE, null, buttons, buttons[0]);

		if( confirm == JOptionPane.CANCEL_OPTION )
		{
			return;
		}
		else if( confirm == JOptionPane.NO_OPTION )
		{
			actionAfterSave.run();
			return;
		}

		// We must save then run the runnable
		GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct()
			{
				currentEditor.doSave();
				return null;
			}

			@Override
			public void finished()
			{
				actionAfterSave.run();
			}

			@Override
			public void exception()
			{
				JOptionPane.showMessageDialog(getComponent(),
					CurrentLocale.get("com.tle.admin.gui.common.tree.nodeeditor.saveerror"));
				getException().printStackTrace();
			}
		};
		worker.setComponent(this);
		worker.start();
	}
}
