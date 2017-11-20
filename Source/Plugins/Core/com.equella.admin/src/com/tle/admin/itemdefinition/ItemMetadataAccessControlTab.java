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

package com.tle.admin.itemdefinition;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.dytech.edge.admin.script.ScriptPanel;
import com.dytech.edge.admin.script.options.ScriptOptions;
import com.dytech.edge.admin.script.options.ScriptOptionsWrapper;
import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.WizardTree;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.admin.wizard.walkers.IterateControls;
import com.dytech.gui.ChangeDetector;
import com.dytech.gui.TableLayout;
import com.tle.admin.controls.scripting.BasicModel;
import com.tle.admin.gui.common.ListWithView;
import com.tle.admin.gui.common.ListWithViewInterface;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.ItemMetadataRule;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.accesscontrolbuilder.AccessEditor;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.ItemMetadataTarget;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;
import com.tle.common.security.remoting.RemoteTLEAclManager;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class ItemMetadataAccessControlTab extends AbstractItemdefTab
{
	private static final String UNTITLED = CurrentLocale
		.get("com.tle.admin.itemdefinition.itemmetadataaccesscontroltab.untitled"); //$NON-NLS-1$

	private final WizardTree tree;
	private final ItemEditor itemEditor;

	private ListWithView<ItemMetadataRule, Editor> listWithView;
	private Map<String, TargetList> targetLists;

	public ItemMetadataAccessControlTab(WizardTree tree, ItemEditor itemEditor)
	{
		this.tree = tree;
		this.itemEditor = itemEditor;
	}

	@Override
	public void init(Component parent)
	{
		listWithView = new ListWithView<ItemMetadataRule, Editor>(true)
		{
			private static final long serialVersionUID = 1L;
			private final Editor editor = new Editor();

			@Override
			protected ItemMetadataRule createElement()
			{
				ItemMetadataRule rule = new ItemMetadataRule();
				rule.setId(UUID.randomUUID().toString());
				rule.setName(UNTITLED);
				return rule;
			}

			@Override
			protected Editor getEditor(ItemMetadataRule currentSelection)
			{
				if( currentSelection == null )
				{
					return null;
				}
				else
				{
					return editor;
				}
			}
		};

		listWithView.setListCellRenderer(new DefaultListCellRenderer()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				String name = ((ItemMetadataRule) value).getName();
				if( Check.isEmpty(name) )
				{
					name = UNTITLED;
				}
				setText(name);

				return this;
			}
		});

		setLayout(new GridLayout(1, 1));
		add(listWithView);
	}

	@Override
	public void validation()
	{
		// Nothing to do here
	}

	@Override
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.itemmetadataaccesscontroltab.title"); //$NON-NLS-1$
	}

	@Override
	public void load()
	{
		listWithView.load(state.getEntity().getItemMetadataRules());

		// Load the given target lists
		targetLists = new HashMap<String, TargetList>();
		final EntityPack<ItemDefinition> entityPack = state.getEntityPack();
		if( entityPack.getOtherTargetLists() != null )
		{
			for( Map.Entry<Object, TargetList> entry : entityPack.getOtherTargetLists().entrySet() )
			{
				if( entry.getKey() instanceof ItemMetadataTarget )
				{
					ItemMetadataTarget target = (ItemMetadataTarget) entry.getKey();
					targetLists.put(target.getId(), entry.getValue());
				}
			}
		}
	}

	@Override
	public void save()
	{
		final ItemDefinition itemDefinition = state.getEntity();
		final EntityPack<ItemDefinition> entityPack = state.getEntityPack();

		itemDefinition.setItemMetadataRules(listWithView.save());

		Map<Object, TargetList> otherTargetLists = entityPack.getOtherTargetLists();
		if( otherTargetLists == null )
		{
			otherTargetLists = new HashMap<Object, TargetList>();
			entityPack.setOtherTargetLists(otherTargetLists);
		}
		else
		{
			for( Iterator<Object> iter = otherTargetLists.keySet().iterator(); iter.hasNext(); )
			{
				if( iter.next() instanceof ItemMetadataTarget )
				{
					iter.remove();
				}
			}
		}

		for( Map.Entry<String, TargetList> entry : targetLists.entrySet() )
		{
			ItemMetadataTarget target = new ItemMetadataTarget(entry.getKey(), itemDefinition);
			otherTargetLists.put(target, entry.getValue());
		}
	}

	/**
	 * @author Nicholas Read
	 */
	private class Editor extends JPanel implements ListWithViewInterface<ItemMetadataRule>
	{
		private static final long serialVersionUID = 1L;
		private JTextField name;
		private JTabbedPane tabs;
		private ScriptPanel scriptView;
		private AccessEditor aclEditor;
		private ChangeDetector changeDetector;

		public Editor()
		{
			super();
		}

		@Override
		public Component getComponent()
		{
			return this;
		}

		@Override
		public void setup()
		{
			if( name != null )
			{
				// Already initialised
				return;
			}

			JLabel nameLabel = new JLabel(
				CurrentLocale.get("com.tle.admin.itemdefinition.itemmetadataaccesscontroltab.name")); //$NON-NLS-1$

			name = new JTextField();

			ScriptOptions myOptions = new ScriptOptionsWrapper(options)
			{
				@Override
				public boolean hasUserIsModerator()
				{
					return false;
				}
				
				@Override
				public boolean hasWorkflow()
				{
					return false;
				}
			};

			scriptView = new ScriptPanel(new BasicModel(schema, myOptions, null)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected List<Control> getControls()
				{
					IterateControls walker = new IterateControls();
					walker.execute(WizardHelper.getRoot(tree.getRoot()));
					return walker.getControls();
				}
			}, itemEditor);

			JPanel scriptBorder = new JPanel(new GridLayout(1, 1));
			scriptBorder.setBorder(AppletGuiUtils.DEFAULT_BORDER);
			scriptBorder.add(scriptView);

			aclEditor = new AccessEditor(clientService.getService(RemoteTLEAclManager.class),
				clientService.getService(RemoteUserService.class));
			aclEditor.setBorder(AppletGuiUtils.DEFAULT_BORDER);

			tabs = new JTabbedPane();
			tabs.addTab(CurrentLocale.get("com.tle.admin.itemdefinition.itemmetadataaccesscontroltab.matches"), //$NON-NLS-1$
				scriptBorder);
			tabs.addTab(CurrentLocale.get("com.tle.admin.itemdefinition.itemmetadataaccesscontroltab.apply"), aclEditor); //$NON-NLS-1$

			final int width1 = nameLabel.getPreferredSize().width;
			final int height1 = name.getPreferredSize().height;

			final int[] rows = {height1, TableLayout.FILL,};
			final int[] cols = {width1, TableLayout.FILL,};

			setLayout(new TableLayout(rows, cols));
			add(nameLabel, new Rectangle(0, 0, 1, 1));
			add(name, new Rectangle(1, 0, 1, 1));
			add(tabs, new Rectangle(0, 1, 2, 1));

			changeDetector = new ChangeDetector();
			changeDetector.watch(scriptView);
			changeDetector.watch(aclEditor);
		}

		@Override
		public void addNameListener(KeyListener listener)
		{
			name.addKeyListener(listener);
		}

		@Override
		public void load(ItemMetadataRule element)
		{
			name.setText(element.getName());
			scriptView.importScript(element.getScript());

			ItemMetadataTarget domainObj = new ItemMetadataTarget(element.getId(), state.getEntity());
			aclEditor.load(domainObj, targetLists.get(element.getId()), Node.ITEM_METADATA);
		}

		@Override
		public void save(ItemMetadataRule element)
		{
			element.setName(name.getText());
			element.setScript(scriptView.getScript());
			targetLists.put(element.getId(), aclEditor.save());
		}

		@Override
		public boolean hasDetectedChanges()
		{
			return changeDetector.hasDetectedChanges();
		}

		@Override
		public void clearChanges()
		{
			changeDetector.clearChanges();
		}
	}
}
