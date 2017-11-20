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

package com.dytech.edge.admin.script.basicmodel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import com.dytech.edge.admin.script.options.ScriptOptions;
import com.tle.admin.helper.GeneralDialog;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SchemaNode;
import com.tle.admin.schema.SchemaTree;
import com.tle.common.i18n.CurrentLocale;

public class ScriptTargetDialog extends GeneralDialog
{
	public static enum SelectionType
	{
		ITEM_STATUS, MODERATION, SCHEMA_ITEM, USER_TYPE, WORKFLOW_STEP;
	}

	private SelectionType type;
	private JRadioButton status;
	private JRadioButton moderation;
	private JRadioButton usertype;
	private JRadioButton workflow;
	private JRadioButton schema;
	private SchemaTree tree;
	private SchemaModel model;
	private JPanel mainPanel;
	private final ScriptOptions options;

	public ScriptTargetDialog(Component parent, SchemaModel model, ScriptOptions options)
	{
		super(parent, getTitle());
		this.model = model;
		this.options = options;
		setInner(createGUI());
	}

	public void showing()
	{
		tree.clearSelection();
	}

	@Override
	protected void onOk()
	{
		if( schema.isSelected() )
		{
			if( tree.isSelectionEmpty() )
			{
				JOptionPane.showMessageDialog(mainPanel, "You must select a schema node from the tree");
				return;
			}
			else if( ((SchemaNode) tree.getLastSelectedPathComponent()).isRoot() )
			{
				JOptionPane.showMessageDialog(mainPanel, "You cannot select the root node.");
				return;
			}
		}
		super.onOk();
	}

	@Override
	public void ok()
	{
		if( status.isSelected() )
		{
			type = SelectionType.ITEM_STATUS;
		}
		else if( moderation.isSelected() )
		{
			type = SelectionType.MODERATION;
		}
		else if( usertype.isSelected() )
		{
			type = SelectionType.USER_TYPE;
		}
		else if( workflow.isSelected() )
		{
			type = SelectionType.WORKFLOW_STEP;
		}
		else if( schema.isSelected() )
		{
			type = SelectionType.SCHEMA_ITEM;
			getSchemaItem();
		}
	}

	protected void getSchemaItem()
	{
		if( tree.isSelectionEmpty() )
		{
			setValue(null);
		}
		else
		{
			setValue(tree.getLastSelectedPathComponent());
		}
	}

	@Override
	public void cancelled()
	{
		type = null;
		setValue(null);
	}

	public void addItem(Object item)
	{
		// We don't want people to be able to add items.
	}

	public void removeItem(Object item)
	{
		// We don't want people to be able to remove items.
	}

	public void clearItems()
	{
		// We don't want people to be able to clear items.
	}

	public SelectionType getType()
	{
		return type;
	}

	private Component createGUI()
	{
		if( model == null )
		{
			tree = new SchemaTree();
		}
		else
		{
			tree = new SchemaTree(model);
		}
		tree.setEditable(false);

		ButtonGroup group = new ButtonGroup();

		DisableTree disabler = new DisableTree();

		status = new JRadioButton(
			CurrentLocale.get("com.dytech.edge.admin.script.schemadialog.status"), options.hasItemStatus()); //$NON-NLS-1$
		status.addActionListener(disabler);
		group.add(status);

		moderation = new JRadioButton(CurrentLocale.get("com.dytech.edge.admin.script.schemadialog.moderator")); //$NON-NLS-1$
		moderation.addActionListener(disabler);
		group.add(moderation);

		usertype = new JRadioButton(CurrentLocale.get("com.dytech.edge.admin.script.schemadialog.role")); //$NON-NLS-1$
		usertype.addActionListener(disabler);
		group.add(usertype);

		workflow = new JRadioButton(CurrentLocale.get("com.dytech.edge.admin.script.schemadialog.workflow")); //$NON-NLS-1$
		workflow.addActionListener(disabler);
		group.add(workflow);

		schema = new JRadioButton(CurrentLocale.get("com.dytech.edge.admin.script.schemadialog.schema")); //$NON-NLS-1$
		schema.addActionListener(new EnableTree());
		group.add(schema);

		JScrollPane scroller = new JScrollPane(tree);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		if( options.hasItemStatus() )
		{
			mainPanel.add(status);
		}
		if( options.hasUserIsModerator() )
		{
			mainPanel.add(moderation);
		}
		mainPanel.add(usertype);

		if( options.hasWorkflow() )
		{
			mainPanel.add(workflow);
		}

		mainPanel.add(schema);
		mainPanel.add(scroller);

		setSize(300, 500);
		enableTree(false);

		return mainPanel;
	}

	protected void enableTree(boolean b)
	{
		tree.setEnabled(b);

	}

	protected class DisableTree implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			enableTree(false);
		}
	}

	protected class EnableTree implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			enableTree(true);
		}
	}

	private static String getTitle()
	{
		return CurrentLocale.get("com.dytech.edge.admin.script.schemadialog.title"); //$NON-NLS-1$
	}
}
