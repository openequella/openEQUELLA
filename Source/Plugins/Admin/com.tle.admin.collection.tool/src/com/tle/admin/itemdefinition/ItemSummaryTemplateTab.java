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
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.thoughtworks.xstream.XStream;
import com.tle.admin.collection.summarydisplay.NewDisplaySummarySectionDialog;
import com.tle.admin.collection.summarydisplay.NoConfigurationConfig;
import com.tle.admin.collection.summarydisplay.SummaryDisplayConfig;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.ListWithView;
import com.tle.admin.gui.common.ListWithViewInterface;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.DisplayNode;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.SummaryDisplayTemplate;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.plugins.PluginTracker;

@SuppressWarnings("nls")
public class ItemSummaryTemplateTab extends AbstractItemdefTab
{
	private PluginTracker<SummaryDisplayConfig> tracker;
	private ListWithView<SummarySectionsConfig, SummaryDisplayView> displayNodeList;

	private JCheckBox hideOwner;
	private JCheckBox hideCollaborators;
	private XStream xstream;

	@Override
	public void init(Component parent)
	{
		tracker = new PluginTracker<SummaryDisplayConfig>(pluginService, "com.tle.admin.collection.tool",
			"summaryDisplay", "id");

		displayNodeList = new ListWithView<SummarySectionsConfig, SummaryDisplayView>(true)
		{
			@Override
			protected SummarySectionsConfig createElement()
			{
				NewDisplaySummarySectionDialog ndssd = new NewDisplaySummarySectionDialog(pluginService);
				return ndssd.showDialog(this);
			}

			@Override
			protected ListWithViewInterface<SummarySectionsConfig> getEditor(SummarySectionsConfig currentSelection)
			{
				return currentSelection == null ? null : new SummaryDisplayView(tracker, currentSelection.getValue());
			}
		};

		displayNodeList.setListCellRenderer(new DefaultListCellRenderer()
		{
			private final String UNTITLED = CurrentLocale
				.get("com.tle.admin.itemdefinition.abstracttemplatetab.untitlednode");

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				String label = CurrentLocale.get(((SummarySectionsConfig) value).getBundleTitle(), null);
				if( Check.isEmpty(label) )
				{
					label = UNTITLED;
				}
				setText(label);

				return this;
			}
		});

		hideOwner = new JCheckBox(CurrentLocale.get("com.tle.admin.collection.tool.summarysections.hideowner"));
		hideCollaborators = new JCheckBox(
			CurrentLocale.get("com.tle.admin.collection.tool.summarysections.hidecollaborators"));

		setLayout(new MigLayout("fill,wrap 1", "[fill,grow]"));
		add(hideOwner);
		add(hideCollaborators);
		add(new JSeparator());
		add(displayNodeList);
	}

	@Override
	public void validation() throws EditorException
	{
		List<SummarySectionsConfig> sections = displayNodeList.save();

		if( sections == null || sections.size() == 0 )
		{
			throw new EditorException(
				CurrentLocale.get("com.tle.admin.collection.tool.itemsummarytemplatetab.validation.nodisplaynodes"));
		}
		else
		{
			for( SummarySectionsConfig section : sections )
			{
				LanguageBundle bundleTitle = section.getBundleTitle();
				if( bundleTitle == null )
				{
					throw new EditorException(
						CurrentLocale.get("com.tle.admin.collection.tool.itemsummarytemplatetab.validation.emptytitle"));
				}

				if( section.getValue().equals("displayNodes") )
				{
					xstream = new XStream();
					xstream.setClassLoader(getClass().getClassLoader());

					Object fromXML = xstream.fromXML(section.getConfiguration());
					List<DisplayNode> displayNodes = (List<DisplayNode>) fromXML;

					for( DisplayNode displayNode : displayNodes )
					{
						LanguageBundle titleBundle = displayNode.getTitle();
						String node = displayNode.getNode();
						String type = displayNode.getType();
						boolean notTheCaseTheyAllEmpty = !(titleBundle == null && Check.isEmpty(node) && Check
							.isEmpty(type));
						boolean incomplete = titleBundle == null || Check.isEmpty(node) || Check.isEmpty(type);
						// If everything's empty we ignore it (it may be being
						// deleted) but it any of the 3 critical values is
						// present we require that they all are
						if( notTheCaseTheyAllEmpty && incomplete )
						{
							throw new EditorException(
								CurrentLocale
									.get("com.tle.admin.collection.tool.itemsummarytemplatetab.validation.incomplete"));
						}
					}
				}
			}
		}
	}

	@Override
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.itemsummarytemplatetab.title");
	}

	@Override
	public void save()
	{
		final ItemDefinition itemDefinition = state.getEntity();
		SummaryDisplayTemplate itemSummaryTemplate = itemDefinition.getItemSummaryDisplayTemplate();
		if( itemSummaryTemplate == null )
		{
			itemSummaryTemplate = new SummaryDisplayTemplate();
			itemDefinition.setItemSummaryDisplayTemplate(itemSummaryTemplate);
		}

		itemSummaryTemplate.setConfigList(displayNodeList.save());
		itemSummaryTemplate.setHideOwner(hideOwner.isSelected());
		itemSummaryTemplate.setHideCollaborators(hideCollaborators.isSelected());
	}

	@Override
	public void load()
	{
		final SummaryDisplayTemplate sdt = state.getEntity().getItemSummaryDisplayTemplate();
		List<SummarySectionsConfig> sections = sdt == null ? null : sdt.getConfigList();

		if( Check.isEmpty(sections) )
		{
			sections = SummarySectionsConfig.createDefaultConfigs();
		}

		displayNodeList.load(sections);
		hideOwner.setSelected(sdt != null && sdt.isHideOwner());
		hideCollaborators.setSelected(sdt != null && sdt.isHideCollaborators());
	}

	public class SummaryDisplayView extends JPanel implements ListWithViewInterface<SummarySectionsConfig>
	{
		private final SummaryDisplayConfig config;

		public SummaryDisplayView(PluginTracker<SummaryDisplayConfig> tracker, String displayId)
		{
			final Extension ext = tracker.getExtension(displayId);
			final Parameter param = ext.getParameter("class");

			config = param == null ? new NoConfigurationConfig() : tracker.getBeanByParameter(ext, param);
			config.setState(state);
			config.setClientService(clientService);
		}

		@Override
		public void addNameListener(KeyListener listener)
		{
			// We don't edit names
		}

		@Override
		public Component getComponent()
		{
			return config.getComponent();
		}

		@Override
		public void load(SummarySectionsConfig element)
		{
			config.load(element);
		}

		@Override
		public void save(SummarySectionsConfig element)
		{
			config.save(element);
		}

		@Override
		public void setup()
		{
			config.setSchemaModel(schema);
			config.setup();
		}

		@Override
		public void clearChanges()
		{
			config.clearChanges();
		}

		@Override
		public boolean hasDetectedChanges()
		{
			return config.hasDetectedChanges();
		}
	}
}
