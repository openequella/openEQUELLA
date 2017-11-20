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

package com.tle.admin.schema;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.tree.TreeSelectionModel;

import com.dytech.devlib.PropBagEx;
import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;
import com.tle.admin.gui.common.AbstractTreeWithView;
import com.tle.admin.gui.common.GenericTreeModel;
import com.tle.admin.gui.common.GenericTreeModel.UpDownDepthFirstPolicy;
import com.tle.admin.gui.common.GenericTreeModel.UpDownPolicyImplementor;
import com.tle.admin.gui.common.TreeWithViewInterface;
import com.tle.admin.gui.common.actions.AddAttributeAction;
import com.tle.admin.gui.common.actions.AddChildAction;
import com.tle.admin.gui.common.actions.AddSiblingAction;
import com.tle.admin.gui.common.actions.RemoveAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.i18n.CurrentLocale;

@SuppressWarnings("nls")
public abstract class AbstractSchemaEditor extends JPanel implements Changeable
{
	public static final String NEW_NAME_PREFIX = "new_element";
	public static final String NO_NAME_PREFIX = "unnamed_element";

	private final SchemaModel model;
	private AbstractTreeWithView<SchemaNode, TreeWithViewInterface<SchemaNode>> treeWithView;

	private ChangeDetector changeDetector;

	public AbstractSchemaEditor()
	{
		this(new SchemaModel());
	}

	public AbstractSchemaEditor(SchemaModel model)
	{
		this.model = model;
		model.setUpDownPolicyImplementor(upDownPolicy);

		setup();
	}

	private void setup()
	{
		treeWithView = new AbstractTreeWithView<SchemaNode, TreeWithViewInterface<SchemaNode>>(model)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected List<TLEAction> getAdditionalActions()
			{
				List<TLEAction> actions = new ArrayList<TLEAction>();
				actions.add(addChildAction);
				actions.add(addSiblingAction);
				actions.add(addAttributeAction);
				actions.add(removeAction);
				return actions;
			}

			@Override
			protected TreeWithViewInterface<SchemaNode> getEditor(SchemaNode currentSelection)
			{
				if( currentSelection == null )
				{
					return null;
				}
				else
				{
					return generateEditor();
				}
			}

			@Override
			protected Component getNoSelectionComponent()
			{
				JLabel label = new JLabel(CurrentLocale.get("com.tle.admin.schema.abstractschemaeditor.select"));

				// Centre it!
				label.setVerticalAlignment(SwingConstants.TOP);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				label.setVerticalTextPosition(SwingConstants.TOP);
				label.setHorizontalTextPosition(SwingConstants.CENTER);

				return label;
			}

			private final TLEAction addChildAction = new AddChildAction()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e)
				{
					SchemaNode parent = getCurrentTreeSelection();
					SchemaNode child = new SchemaNode(generateUniqueName(parent));
					model.add(child, parent);
					setTreeSelection(child);
				}

				@Override
				public void update()
				{
					SchemaNode node = getCurrentTreeSelection();
					setEnabled(treeWithView.isEnabled() && node != null && !node.isAttribute()
						&& (!node.isLocked() || node.hasNonAttributeChildren()));
				}
			};

			private final TLEAction addSiblingAction = new AddSiblingAction()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e)
				{
					SchemaNode parent = (SchemaNode) getCurrentTreeSelection().getParent();
					SchemaNode child = new SchemaNode(generateUniqueName(parent));
					model.add(child, parent);
					setTreeSelection(child);
				}

				@Override
				public void update()
				{
					SchemaNode node = getCurrentTreeSelection();
					setEnabled(treeWithView.isEnabled() && node != null && !node.isRoot() && !node.isAttribute());
				}
			};

			private final TLEAction addAttributeAction = new AddAttributeAction()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e)
				{
					SchemaNode parent = getCurrentTreeSelection();
					SchemaNode attr = new SchemaNode(generateUniqueName(parent));
					attr.setAttribute(true);

					int index = 0;
					int size = parent.getChildCount();
					while( index < size && ((SchemaNode) parent.getChildAt(index)).isAttribute() )
					{
						index++;
					}

					model.insert(attr, parent, index);
					setTreeSelection(attr);
				}

				@Override
				public void update()
				{
					SchemaNode node = getCurrentTreeSelection();
					setEnabled(treeWithView.isEnabled() && node != null && !node.isAttribute());
				}
			};

			private final TLEAction removeAction = new RemoveAction()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e)
				{
					String message = CurrentLocale.get("schema.prompt.remove.body"); //$NON-NLS-1$

					int confirm = JOptionPane.showConfirmDialog(AbstractSchemaEditor.this, message,
						CurrentLocale.get("schema.prompt.remove.title"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

					if( confirm == JOptionPane.YES_OPTION )
					{
						model.remove(getCurrentTreeSelection());
						setTreeSelection(null);
					}
				}

				@Override
				public void update()
				{
					SchemaNode node = getCurrentTreeSelection();
					setEnabled(treeWithView.isEnabled() && node != null && !node.isRoot() && !node.isLocked());
				}
			};
		};

		treeWithView.initialise(true, 200);
		treeWithView.setTreeCellRenderer(new SchemaRenderer(false));
		treeWithView.setTreeSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		setLayout(new GridLayout(1, 1));
		add(treeWithView);

		changeDetector = new ChangeDetector();
		changeDetector.watch(model.getUnderlyingTreeModel());
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.Changeable#hasDetectedChanges()
	 */
	@Override
	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.Changeable#clearChanges()
	 */
	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		treeWithView.setEnabled(enabled);
	}

	/**
	 * Accessor method for JTree.
	 * 
	 * @return The tree model.
	 */
	public SchemaModel getModel()
	{
		return model;
	}

	public PropBagEx getXml()
	{
		// Make sure 'current' field is saved
		treeWithView.save();
		return model.getXml();
	}

	public void loadSchema(PropBagEx xml)
	{
		model.loadSchema(xml);
	}

	/**
	 * Generates a new unique name for a child of the given parent.
	 */
	private String generateUniqueName(SchemaNode parent)
	{
		int i = 0;
		String name = NEW_NAME_PREFIX;
		while( parent.hasChild(name) )
		{
			i++;
			name = NEW_NAME_PREFIX + '_' + i;
		}
		return name;
	}

	protected abstract TreeWithViewInterface<SchemaNode> generateEditor();

	private final UpDownPolicyImplementor<SchemaNode> upDownPolicy = new UpDownDepthFirstPolicy<SchemaNode>()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean canMoveDown(GenericTreeModel<SchemaNode> model, SchemaNode node)
		{
			return !node.isLocked() && super.canMoveDown(model, node);
		}

		@Override
		public boolean canMoveUp(GenericTreeModel<SchemaNode> model, SchemaNode node)
		{
			return !node.isLocked() && super.canMoveUp(model, node);
		}

		@Override
		protected boolean shouldDescendInto(GenericTreeModel<SchemaNode> model, SchemaNode node,
			SchemaNode possibleParent)
		{
			return possibleParent.getChildCount() > 0;
		}
	};
}
