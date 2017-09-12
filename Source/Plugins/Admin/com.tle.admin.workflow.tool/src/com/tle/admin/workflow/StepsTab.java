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

package com.tle.admin.workflow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.dytech.edge.gui.workflow.WorkflowVisualiser;
import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.tle.admin.Driver;
import com.tle.admin.baseentity.BaseEntityEditor.AbstractDetailsTab;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.actions.AddAction;
import com.tle.admin.gui.common.actions.DownAction;
import com.tle.admin.gui.common.actions.EditAction;
import com.tle.admin.gui.common.actions.JTextlessButton;
import com.tle.admin.gui.common.actions.PreviewAction;
import com.tle.admin.gui.common.actions.RemoveAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.gui.common.actions.UpAction;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.workflow.tree.WorkflowTree;
import com.tle.admin.workflow.tree.WorkflowTreeModel;
import com.tle.beans.entity.LanguageBundle;
import com.tle.client.gui.popup.TreeDoubleClickListener;
import com.tle.client.gui.popup.TreePopupListener;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.recipientselector.SingleUserSelector;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.node.ScriptNode;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.common.workflow.node.WorkflowTreeNode;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.core.remoting.RemoteUserService;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
public class StepsTab extends BaseEntityTab<Workflow> implements AbstractDetailsTab<Workflow>
{
	private WorkflowTree tree;
	private WorkflowTreeModel model;

	private JCheckBox moveLive;
	private I18nTextField nameField;
	private SingleUserSelector owner;

	private final List<TLEAction> actions = new ArrayList<TLEAction>();

	private boolean hideRemoveWarning = false;

	@Override
	public void init(Component parent)
	{
		actions.add(addAction);
		actions.add(modifyAction);
		actions.add(removeAction);
		actions.add(upAction);
		actions.add(downAction);
		actions.add(previewAction);

		createGUI();
	}

	@Override
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.workflow.stepstab.title"); //$NON-NLS-1$
	}

	@Override
	public void addNameListener(final KeyListener listener)
	{
		nameField.addKeyListener(listener);
	}

	@Override
	public void validation() throws EditorException
	{
		if( nameField.isCompletelyEmpty() )
		{
			throw new EditorException(CurrentLocale.get("com.tle.admin.workflow.stepstab.supplyname")); //$NON-NLS-1$
		}

		if( owner.getUser() == null )
		{
			throw new EditorException(CurrentLocale.get("com.tle.admin.workflow.stepstab.noowner")); //$NON-NLS-1$
		}

		final WorkflowTreeNode node = model.getRootNode();
		recurseValidate(node);
	}

	private void recurseValidate(final WorkflowTreeNode pNode) throws EditorException
	{
		final Iterator<WorkflowNode> i = pNode.iterateChildren();
		while( i.hasNext() )
		{
			final WorkflowNode node = i.next();

			if( LangUtils.isEmpty(node.getName()) )
			{
				tree.setSelectionPath(new TreePath(model.getPathToRoot(node)));
				throw new EditorException(CurrentLocale.get("com.tle.admin.workflow.stepstab.supplystepname")); //$NON-NLS-1$
			}

			if( node.getType() == WorkflowNode.ITEM_TYPE )
			{
				final WorkflowItem item = (WorkflowItem) node;
				if( !item.isModeratorsSpecified() )
				{
					tree.setSelectionPath(new TreePath(model.getPathToRoot(item)));
					throw new EditorException(CurrentLocale.get("com.tle.admin.workflow.stepstab.selected")); //$NON-NLS-1$
				}
			}

			if( node.getType() == WorkflowNode.SCRIPT_TYPE )
			{
				final ScriptNode scriptNode = (ScriptNode) node;
				if( scriptNode.isNotifyOnCompletion() && !scriptNode.isNotifyOnCompletionSpecified() )
				{
					tree.setSelectionPath(new TreePath(model.getPathToRoot(scriptNode)));
					throw new EditorException(
						CurrentLocale
							.get("com.tle.admin.workflow.script.scripteditor.nofificationstab.completion.notify.nobody"));
				}

				if( !scriptNode.isNotifyNoErrorSpecified() )
				{
					tree.setSelectionPath(new TreePath(model.getPathToRoot(scriptNode)));
					throw new EditorException(
						CurrentLocale
							.get("com.tle.admin.workflow.script.scripteditor.nofificationstab.error.notify.nobody"));
				}
			}

			if( node instanceof WorkflowTreeNode )
			{
				recurseValidate((WorkflowTreeNode) node);
			}
		}
	}

	private void createGUI()
	{
		final JComponent north = createNorth();
		final JComponent centre = createCentre();

		setLayout(new BorderLayout(5, 5));

		add(north, BorderLayout.NORTH);
		add(centre, BorderLayout.CENTER);

		updateButtons();
	}

	private JComponent createNorth()
	{
		final JLabel nameLabel = new JLabel(CurrentLocale.get("com.tle.admin.workflow.stepstab.name")); //$NON-NLS-1$
		final JLabel ownerLabel = new JLabel(CurrentLocale.get("com.tle.admin.workflow.stepstab.owner")); //$NON-NLS-1$

		nameField = new I18nTextField(BundleCache.getLanguages());

		owner = new SingleUserSelector(clientService.getService(RemoteUserService.class));

		moveLive = new JCheckBox(CurrentLocale.get("com.tle.admin.workflow.stepstab.live")); //$NON-NLS-1$

		final int height1 = owner.getPreferredSize().height;
		final int width1 = ownerLabel.getPreferredSize().width;
		final int[] rows = {height1, height1, height1, height1,};
		final int[] cols = {width1, TableLayout.FILL, TableLayout.FILL,};

		final JPanel north = new JPanel(new TableLayout(rows, cols));
		north.add(nameLabel, new Rectangle(0, 0, 1, 1));
		north.add(nameField, new Rectangle(1, 0, 1, 1));
		north.add(ownerLabel, new Rectangle(0, 1, 1, 1));
		north.add(owner, new Rectangle(1, 1, 1, 1));
		north.add(moveLive, new Rectangle(0, 2, 3, 1));
		north.add(new JSeparator(), new Rectangle(0, 3, 3, 1));

		return north;
	}

	private JComponent createCentre()
	{
		final JButton add = new JButton(addAction);
		final JButton modify = new JButton(modifyAction);
		final JButton delete = new JButton(removeAction);
		final JButton preview = new JButton(previewAction);
		final JButton up = new JTextlessButton(upAction);
		final JButton down = new JTextlessButton(downAction);

		model = new WorkflowTreeModel();
		tree = new WorkflowTree(model, clientService.getService(RemoteUserService.class),
			clientService.getService(RemoteSchemaService.class));
		tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(final TreeSelectionEvent e)
			{
				updateButtons();
			}
		});
		tree.addMouseListener(new TreeDoubleClickListener(tree, modifyAction));
		tree.addMouseListener(new TreePopupListener(tree, actions));

		final JScrollPane scroll = new JScrollPane(tree);
		scroll.getViewport().setBackground(Color.WHITE);

		final int width1 = up.getPreferredSize().width;
		final int width2 = Math.max(preview.getPreferredSize().width, delete.getPreferredSize().width);
		final int height1 = up.getPreferredSize().height;
		final int height2 = delete.getPreferredSize().height;

		final int[] rows = {TableLayout.FILL, height1, height1, TableLayout.FILL, height2};
		final int[] cols = {width1, TableLayout.FILL, width2, width2, width2, width2, TableLayout.FILL,};

		final JPanel buttons = new JPanel(new TableLayout(rows, cols));

		int row = 0;
		buttons.add(scroll, new Rectangle(1, row++, 6, 4));
		buttons.add(up, new Rectangle(0, row++, 1, 1));
		buttons.add(down, new Rectangle(0, row++, 1, 1));
		row++;
		buttons.add(add, new Rectangle(2, row, 1, 1));
		buttons.add(modify, new Rectangle(3, row, 1, 1));
		buttons.add(delete, new Rectangle(4, row, 1, 1));
		buttons.add(preview, new Rectangle(5, row, 1, 1));

		return buttons;
	}

	private void updateButtons()
	{
		for( final TLEAction action : actions )
		{
			action.update();
		}
	}

	private final TLEAction addAction = new AddAction()
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			tree.add();
		}

		@Override
		public void update()
		{
			final WorkflowNode node = tree.getSelectedNode();
			setEnabled(node != null && node.canAddChildren());
		}
	};

	private final TLEAction modifyAction = new EditAction()
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			tree.modify();
		}

		@Override
		public void update()
		{
			final WorkflowNode node = tree.getSelectedNode();
			setEnabled(node != null && node.getParent() != null);
		}
	};

	private final TLEAction removeAction = new RemoveAction()
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			if( hideRemoveWarning || showWarning() )
			{
				tree.remove();
			}
		}

		private boolean showWarning()
		{
			final String prefix = "com.tle.admin.workflow.workfloweditor.removestep."; //$NON-NLS-1$
			final Object yes = CurrentLocale.get(prefix + "yes"); //$NON-NLS-1$
			final Object yesDnwa = CurrentLocale.get(prefix + "yesdnwa"); //$NON-NLS-1$
			final Object cancel = CurrentLocale.get(prefix + "cancel"); //$NON-NLS-1$

			final int result = JOptionPane.showOptionDialog(panel, CurrentLocale.get(prefix + "message"), //$NON-NLS-1$
				CurrentLocale.get(prefix + "title"), JOptionPane.YES_NO_CANCEL_OPTION, //$NON-NLS-1$
				JOptionPane.QUESTION_MESSAGE, null, new Object[]{yes, yesDnwa, cancel}, cancel);

			// Check if the middle option has been chosen
			if( result == JOptionPane.NO_OPTION )
			{
				hideRemoveWarning = true;
			}

			return result != JOptionPane.CANCEL_OPTION;
		}

		@Override
		public void update()
		{
			final WorkflowNode node = tree.getSelectedNode();
			setEnabled(node != null && node.getParent() != null);
		}
	};

	private final TLEAction previewAction = new PreviewAction()
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			try
			{
				validation();
			}
			catch( final EditorException ex )
			{
				Driver.displayInformation(parent, ex.getMessage());
				return;
			}

			final WorkflowVisualiser visualiser = new WorkflowVisualiser(model.getRootNode(), new Dimension(500, 500));

			final ScrollPane scrollpane = new ScrollPane();
			scrollpane.add(visualiser);

			final JDialog dialog = ComponentHelper.createJDialog(panel);
			dialog.setTitle(CurrentLocale.get("com.tle.admin.workflow.stepstab.visualiser")); //$NON-NLS-1$
			dialog.getContentPane().add(scrollpane);
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setSize(500, 500);
			dialog.setModal(true);

			ComponentHelper.centreOnScreen(dialog);
			try
			{
				dialog.setVisible(true);
			}
			catch( final Exception ex )
			{
				JOptionPane.showMessageDialog(panel, CurrentLocale.get("com.tle.admin.workflow.stepstab.notcomplete")); //$NON-NLS-1$
			}
		}

		@Override
		public void update()
		{
			setEnabled(model.getRootNode() != null);
		}
	};

	private final TLEAction upAction = new UpAction()
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			tree.up();
		}

		@Override
		public void update()
		{
			final WorkflowNode node = tree.getSelectedNode();

			boolean canUp = false;
			if( node != null )
			{
				final WorkflowNode parent1 = node.getParent();
				if( parent1 != null )
				{
					final int index = parent1.indexOfChild(node);
					canUp = parent1.getParent() != null || index != 0;
				}

			}
			setEnabled(canUp);
		}
	};

	private final TLEAction downAction = new DownAction()
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			tree.down();
		}

		@Override
		public void update()
		{
			final WorkflowNode node = tree.getSelectedNode();

			boolean canDown = false;
			if( node != null )
			{
				final WorkflowNode parent1 = node.getParent();
				if( parent1 != null )
				{
					final int index = parent1.indexOfChild(node);
					canDown = parent1.getParent() != null || index != parent1.numberOfChildren() - 1;
				}

			}
			setEnabled(canDown);
		}
	};

	@Override
	public void save()
	{
		final Workflow workflow = state.getEntity();

		LanguageBundle oldWorkflowNameBundle = workflow.getName();
		LanguageBundle savedNameBundle = nameField.save();
		workflow.setName(savedNameBundle);

		// Clear the old Workflow name from the BundleCache, if it was there.
		// Any process (such as
		// the metadata editor) looking to display the workflow name will be
		// required to fetch the new one.
		if( oldWorkflowNameBundle != null && !oldWorkflowNameBundle.equals(savedNameBundle) )
		{
			BundleCache.invalidate(oldWorkflowNameBundle.getId());
		}

		workflow.setOwner(owner.getUser().getUniqueID());
		workflow.setMovelive(moveLive.isSelected());

		final WorkflowTreeNode node = model.getRootNode();
		workflow.setRoot(node);
	}

	@Override
	public void load()
	{
		final Workflow workflow = state.getEntity();

		nameField.load(workflow.getName());
		owner.setUserId(workflow.getOwner());
		moveLive.setSelected(workflow.isMovelive());

		model.setRoot((WorkflowTreeNode) workflow.getRoot());
		tree.updateUI();
	}
}
