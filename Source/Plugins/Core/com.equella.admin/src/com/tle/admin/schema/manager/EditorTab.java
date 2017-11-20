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

package com.tle.admin.schema.manager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.JSmartTextField;
import com.dytech.gui.LineBorder;
import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.TreeWithViewInterface;
import com.tle.admin.schema.AbstractSchemaEditor;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SchemaNode;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.Schema;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
public class EditorTab extends BaseEntityTab<Schema>
{
	private final SchemaModel schemaModel;

	private AbstractSchemaEditor editor;
	private JPanel lockedPanel;
	private boolean locked;

	public EditorTab(SchemaModel schemaModel)
	{
		this.schemaModel = schemaModel;
	}

	@Override
	public void init(Component parent)
	{
		setupGUI();
	}

	@Override
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.schema.manager.editortab.title"); //$NON-NLS-1$
	}

	public void addToChangeListener(ChangeDetector changeDetector)
	{
		changeDetector.watch(editor);
	}

	private void setupGUI()
	{
		editor = new AbstractSchemaEditor(schemaModel)
		{
			@Override
			protected TreeWithViewInterface<SchemaNode> generateEditor()
			{
				return new Editor();
			}
		};

		setLayout(new BorderLayout(5, 5));
		add(createLockedArea(), BorderLayout.NORTH);
		add(editor, BorderLayout.CENTER);

		// Make sure things are readonly.
		if( state.isReadonly() )
		{
			editor.setEnabled(false);
		}
	}

	private JComponent createLockedArea()
	{
		JLabel label = new JLabel(CurrentLocale.get("com.tle.admin.schema.manager.editortab.locked")); //$NON-NLS-1$

		lockedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lockedPanel.setBorder(new LineBorder(Color.BLACK, 1, 0, 1, 0));
		lockedPanel.setBackground(new Color(254, 254, 200));
		lockedPanel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				GlassSwingWorker<PartiallyLockedDialog> worker = new GlassSwingWorker<PartiallyLockedDialog>()
				{
					@Override
					public PartiallyLockedDialog construct() throws Exception
					{
						Collection<BaseEntityLabel> usages = clientService.getService(RemoteSchemaService.class)
							.getSchemaUses(state.getEntity().getId());
						BundleCache.ensureCached(usages);
						return new PartiallyLockedDialog(usages);
					}

					@Override
					public void finished()
					{
						PartiallyLockedDialog pld = get();
						if( pld.askToUnlock(panel) )
						{
							locked = false;
							updateLockedHeight();

							SchemaNode root = editor.getModel().getRoot();
							unlockNodeTree(root);
							editor.getModel().nodeStructureChanged(root);
						}
					}

					@Override
					public void exception()
					{
						getException().printStackTrace();
					}
				};

				worker.setComponent(panel);
				worker.start();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				panel.setCursor(Cursor.getDefaultCursor());
			}
		});

		lockedPanel.add(label);

		updateLockedHeight();

		return lockedPanel;
	}

	private void updateLockedHeight()
	{
		if( locked )
		{
			lockedPanel.setMinimumSize(null);
			lockedPanel.setPreferredSize(null);
		}
		else
		{
			Dimension zero = new Dimension();
			lockedPanel.setMinimumSize(zero);
			lockedPanel.setPreferredSize(zero);
		}
	}

	@Override
	public void load()
	{
		editor.loadSchema(state.getEntity().getDefinitionNonThreadSafe());

		updateLockedHeight();
		if( state.isLoaded() )
		{
			GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
			{
				@Override
				public Object construct() throws Exception
				{
					locked = !Check.isEmpty(clientService.getService(RemoteSchemaService.class).getReferencingClasses(
						state.getEntity().getId()));
					return null;
				}

				@Override
				public void finished()
				{
					if( locked )
					{
						lockNodeTree(editor.getModel().getRoot());
						updateLockedHeight();
					}
				}
			};
			worker.setComponent(getComponent());
			worker.start();
		}
	}

	@Override
	public void save()
	{
		state.getEntity().setDefinition(editor.getXml());
	}

	@Override
	public void validation() throws EditorException
	{
		SchemaNode root = editor.getModel().getRoot();
		recurse(root);
	}

	@SuppressWarnings("nls")
	private void recurse(SchemaNode node) throws EditorException
	{
		int count = node.getChildCount();
		Set<String> names = new HashSet<String>(count);
		for( int i = 0; i < count; i++ )
		{
			SchemaNode childAt = (SchemaNode) node.getChildAt(i);
			String name = childAt.getName();
			if( !names.add((childAt.isAttribute() ? "@" : "") + name) )
			{
				String type = "nodes";
				if( childAt.isAttribute() )
				{
					type = "attributes";
				}
				throw new EditorException(CurrentLocale.get(
					"com.tle.admin.schema.manager.editortab.warning", new Object[]{type, name, //$NON-NLS-1$
							node.toString()}));
			}
			recurse(childAt);
		}
	}

	/**
	 * Lock the given node, and all of it's children.
	 */
	private void lockNodeTree(SchemaNode node)
	{
		if( !node.isLocked() )
		{
			node.lock();
		}

		int count = node.getChildCount();
		for( int i = 0; i < count; i++ )
		{
			lockNodeTree((SchemaNode) node.getChildAt(i));
		}
	}

	void unlockNodeTree(SchemaNode node)
	{
		node.unlock();

		int count = node.getChildCount();
		for( int i = 0; i < count; i++ )
		{
			unlockNodeTree((SchemaNode) node.getChildAt(i));
		}
	}

	/**
	 * @author Nicholas Read
	 */
	class Editor extends JPanel implements TreeWithViewInterface<SchemaNode>
	{
		private static final long serialVersionUID = 1L;

		private SchemaNode current;

		private JSmartTextField targetName;
		private JComboBox targetType;
		private JCheckBox targetSearch;
		private JCheckBox targetField;

		@Override
		public Component getComponent()
		{
			return this;
		}

		@Override
		public void setup()
		{
			JLabel titleLabel = new JLabel("<html><b>" //$NON-NLS-1$
				+ CurrentLocale.get("com.tle.admin.schema.manager.editortab.target")); //$NON-NLS-1$
			JLabel nameLabel = new JLabel(CurrentLocale.get("com.tle.admin.schema.manager.editortab.name")); //$NON-NLS-1$
			JLabel typeLabel = new JLabel(CurrentLocale.get("com.tle.admin.schema.manager.editortab.type")); //$NON-NLS-1$

			targetName = new JSmartTextField();
			targetName.setStartWithAlphaCharacter(true);
			targetName.setInputType(JSmartTextField.I_ALPHANUMERIC | JSmartTextField.I_PERIOD
				| JSmartTextField.I_UNDERSCORE | JSmartTextField.I_HYPHEN);

			targetType = new JComboBox(new String[]{"text", "html"}); //$NON-NLS-1$  //$NON-NLS-2$
			targetSearch = new JCheckBox(CurrentLocale.get("com.tle.admin.schema.manager.editortab.free")); //$NON-NLS-1$
			targetField = new JCheckBox(CurrentLocale.get("com.tle.admin.schema.manager.editortab.index")); //$NON-NLS-1$

			ActionListener updateListener = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					updateGui();
				}
			};
			targetSearch.addActionListener(updateListener);

			final int width1 = nameLabel.getPreferredSize().width;
			final int height1 = titleLabel.getPreferredSize().height;
			final int height2 = targetName.getPreferredSize().height;

			final int[] rows = {height1, height2, height2, height2, height2, TableLayout.FILL,};
			final int[] columns = {width1, TableLayout.FILL,};

			setLayout(new TableLayout(rows, columns));

			add(titleLabel, new Rectangle(0, 0, 2, 1));
			add(nameLabel, new Rectangle(0, 1, 1, 1));
			add(targetName, new Rectangle(1, 1, 1, 1));
			add(typeLabel, new Rectangle(0, 2, 1, 1));
			add(targetType, new Rectangle(1, 2, 1, 1));
			add(targetSearch, new Rectangle(0, 3, 2, 1));
			add(targetField, new Rectangle(0, 4, 2, 1));
		}

		@Override
		public void addNameListener(KeyListener listener)
		{
			targetName.addKeyListener(listener);
		}

		@Override
		public void load(SchemaNode element)
		{
			current = element;

			targetName.setText(current.getName());
			targetType.setSelectedIndex(current.getType().equals("html") ? 1 : 0); //$NON-NLS-1$
			targetSearch.setSelected(current.isSearchable());
			targetField.setSelected(current.isField());

			updateGui();
		}

		@Override
		public void save(SchemaNode element)
		{
			String name = targetName.getText();
			if( Check.isEmpty(name) )
			{
				name = AbstractSchemaEditor.NO_NAME_PREFIX;
			}
			element.setName(name);

			element.setType(targetType.getSelectedItem().toString());
			element.setSearchable(targetSearch.isSelected());
			element.setField(targetField.isSelected());
		}

		private void updateGui()
		{
			boolean decideEdit = !state.isReadonly() && !current.isLocked() && !current.isRoot();
			targetName.setEnabled(decideEdit);
			targetType.setEnabled(decideEdit);
			targetSearch.setEnabled(decideEdit);
			targetField.setEnabled(decideEdit);
		}
	}
}
