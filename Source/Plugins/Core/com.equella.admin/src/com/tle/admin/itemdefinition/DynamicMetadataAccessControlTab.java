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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.UUID;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ChangeDetector;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.ListWithView;
import com.tle.admin.gui.common.ListWithViewInterface;
import com.tle.admin.schema.SingleTargetChooser;
import com.tle.admin.security.tree.PartialAclEditor;
import com.tle.beans.entity.itemdef.DynamicMetadataRule;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;

@SuppressWarnings("nls")
public class DynamicMetadataAccessControlTab extends AbstractItemdefTab
{
	private static final String UNTITLED = s("untitled");

	private ListWithView<DynamicMetadataRule, DynamicMetadataRuleEditor> listWithView;

	public enum DynamicType
	{
		USER("user"), GROUP("group"), ROLE("role");

		private final String text;

		private DynamicType(String key)
		{
			this.text = s(key);
		}

		public String getText()
		{
			return text;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void init(Component parent)
	{
		listWithView = new ListWithView<DynamicMetadataRule, DynamicMetadataRuleEditor>(true)
		{
			private static final long serialVersionUID = 1L;
			private final DynamicMetadataRuleEditor editor = new DynamicMetadataRuleEditor();

			@Override
			protected DynamicMetadataRule createElement()
			{
				DynamicMetadataRule rule = new DynamicMetadataRule();
				rule.setId(UUID.randomUUID().toString());
				rule.setName(UNTITLED);
				TargetList tl = new TargetList();
				rule.setTargetList(tl);
				return rule;
			}

			@Override
			protected DynamicMetadataRuleEditor getEditor(DynamicMetadataRule currentSelection)
			{
				if( currentSelection == null )
				{
					return null;
				}
				return editor;
			}
		};

		listWithView.setListCellRenderer(new DefaultListCellRenderer()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				String name = ((DynamicMetadataRule) value).getName();
				if( Check.isEmpty(name) )
				{
					name = UNTITLED;
				}
				setText(name);

				return this;
			}
		});

		setLayout(new MigLayout("insets 0"));
		add(listWithView);
	}

	@Override
	public String getTitle()
	{
		return s("title");
	}

	@Override
	public void load()
	{
		listWithView.load(state.getEntity().getDynamicMetadataRules());
	}

	@Override
	public void validation() throws EditorException
	{
		int count = 0;
		for( DynamicMetadataRule rule : getEntries() )
		{
			if( Check.isEmpty(rule.getName()) )
			{
				listWithView.getList().setSelectedIndex(count);
				throw new EditorException(s("invalid.empty.name"));
			}
			if( Check.isEmpty(rule.getPath()) )
			{
				listWithView.getList().setSelectedIndex(count);
				throw new EditorException(s("invalid.empty.path"));
			}
			count++;
		}
	}

	private List<DynamicMetadataRule> getEntries()
	{
		return listWithView.save();
	}

	@Override
	public void save()
	{
		state.getEntity().setDynamicMetadataRules(listWithView.save());
	}

	private static String s(String key)
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.dynamicmetadataaccesscontroltab." + key);
	}

	private class DynamicMetadataRuleEditor extends JPanel implements ListWithViewInterface<DynamicMetadataRule>
	{
		private static final long serialVersionUID = 1L;
		private JTextField name;
		private SingleTargetChooser path;
		private JComboBox<NameValue> type;
		private PartialAclEditor aclEditor;
		private ChangeDetector changeDetector;

		public DynamicMetadataRuleEditor()
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
				return;
			}

			JLabel nameLabel = new JLabel(s("name"));
			JLabel pathLabel = new JLabel(s("path"));
			JLabel pathHelpLabel = new JLabel(s("path.help"));
			pathHelpLabel.setForeground(Color.GRAY);
			JLabel typeLabel = new JLabel(s("type"));
			JLabel applyLabel = new JLabel(s("apply"));

			name = new JTextField();
			path = new SingleTargetChooser(schema, "");
			type = new JComboBox<NameValue>();
			AppletGuiUtils.addItemsToJCombo(type,
				Lists.transform(Lists.newArrayList(DynamicType.values()), new Function<DynamicType, NameValue>()
				{
					@Override
					public NameValue apply(DynamicType type)
					{
						return new NameValue(type.getText(), type.toString());
					}
				}));

			aclEditor = new PartialAclEditor(Node.DYNAMIC_ITEM_METADATA);

			setLayout(new MigLayout("wrap, insets 0", "[][grow, fill]"));
			add(nameLabel);
			add(name);
			add(pathLabel);
			add(path);
			add(pathHelpLabel, "skip 1");
			add(typeLabel);
			add(type);
			add(applyLabel, "span 2");
			add(new JSeparator(), "growx, span 2");
			add(aclEditor, "span 2, grow, push");

			changeDetector = new ChangeDetector();
			changeDetector.watch(aclEditor);
		}

		@Override
		public void addNameListener(KeyListener listener)
		{
			name.addKeyListener(listener);
		}

		@Override
		public void load(DynamicMetadataRule rule)
		{
			name.setText(rule.getName());
			path.setTarget(rule.getPath());
			AppletGuiUtils.selectInJCombo(type, new NameValue("", rule.getType()), 0);
			aclEditor.load(rule.getTargetList());
		}

		@Override
		public void save(DynamicMetadataRule rule)
		{
			rule.setName(name.getText());
			rule.setPath(path.getTarget());
			rule.setType(((NameValue) type.getSelectedItem()).getValue());
			rule.setTargetList(aclEditor.save());
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
