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

package com.tle.admin.search.searchset.virtualisation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.java.plugin.registry.Extension;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.DynamicChoicePanel;
import com.tle.admin.gui.common.RadioButtonChoiceList;
import com.tle.admin.helper.GroupBox;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.search.searchset.SearchSet;
import com.tle.core.plugins.PluginService;

@SuppressWarnings("nls")
public class VirtualisationEditor extends JPanel implements Changeable
{
	private static final String PREFIX = "com.tle.admin.search.searchset.virtualisation.";

	private final ChangeDetector changeDetector;
	private final GroupBox enabledGroup;
	private final JTextField xpathField;
	private final RadioButtonChoiceList<SearchSet, Extension> visualiserConfigs;

	public VirtualisationEditor(final PluginService pluginService, final ClientService clientService,
		final String entityNameSingularKey, final String renamingHelpKey)
	{
		enabledGroup = GroupBox.withCheckBox(CurrentLocale.get(PREFIX + "grouptitle"), false);

		StringBuilder instructions = new StringBuilder();
		instructions.append("<html>");
		instructions.append(CurrentLocale.get(PREFIX + "instructions", CurrentLocale.get(entityNameSingularKey)));
		if( renamingHelpKey != null )
		{
			instructions.append("<br><br>");
			instructions.append(CurrentLocale.get(renamingHelpKey));
		}

		setLayout(new MigLayout("wrap, insets 0", "[grow]", "[][grow]"));
		add(new JLabel(instructions.toString()));
		add(enabledGroup, "grow");

		enabledGroup.getInnerPanel().setLayout(new MigLayout("wrap", "[grow]"));

		// // XPath Selector //////////////////////////////////////////////////

		enabledGroup.add(new JLabel(CurrentLocale.get(PREFIX + "xpath")));
		xpathField = new JTextField();
		xpathField.setEditable(false);
		enabledGroup.add(xpathField, "gap indent, split 2, growx");

		final JButton xpathSelect = new JButton("Select...");
		xpathSelect.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				final SchemaAndTargetChooserDialog dialog = new SchemaAndTargetChooserDialog(clientService,
					xpathSelect, true);
				final String xpath = dialog.showDialog();
				if( !Check.isEmpty(xpath) )
				{
					xpathField.setText(xpath);
				}
			}
		});
		enabledGroup.add(xpathSelect);

		// // Virtualiser Configuration ///////////////////////////////////////

		visualiserConfigs = new RadioButtonChoiceList<SearchSet, Extension>()
		{
			@Override
			public String getChoiceId(Extension choice)
			{
				return choice.getId();
			}

			@Override
			@SuppressWarnings("unchecked")
			public DynamicChoicePanel<SearchSet> getChoicePanel(Extension choice)
			{
				return (DynamicChoicePanel<SearchSet>) pluginService.getBean(choice.getDeclaringPluginDescriptor(),
					choice.getParameter("configPanel").valueAsString());
			}

			@Override
			public String getChoiceTitle(Extension choice)
			{
				return CurrentLocale.get(choice.getParameter("nameKey").valueAsString());
			}

			@Override
			public String getSavedChoiceId(SearchSet state)
			{
				return state.getVirtualiserPluginId();
			}

			@Override
			public void setSavedChoiceId(SearchSet state, String choiceId)
			{
				state.setVirtualiserPluginId(choiceId);
			}
		};
		visualiserConfigs.loadChoices(pluginService.getConnectedExtensions("com.tle.admin.search",
			"searchSetVirtualiserConfigs"));

		enabledGroup.add(new JLabel(CurrentLocale.get(PREFIX + "source")));
		enabledGroup.add(visualiserConfigs, "gap indent, grow");

		// // Change Detection ////////////////////////////////////////////////

		changeDetector = new ChangeDetector();
		changeDetector.watch(xpathField);
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

	public void validation() throws EditorException
	{
		if( enabledGroup.isSelected() )
		{
			if( Check.isEmpty(xpathField.getText()) )
			{
				throw new EditorException(CurrentLocale.get(PREFIX + "xpath.mustselect"));
			}
			if( visualiserConfigs.isSelectionEmpty() )
			{
				throw new EditorException(CurrentLocale.get(PREFIX + "source.validation"));
			}
		}
	}

	public void load(SearchSet searchSet)
	{
		final String path = searchSet.getVirtualisationPath();
		if( path != null )
		{
			enabledGroup.setSelected(true);
			xpathField.setText(path);
			visualiserConfigs.load(searchSet);
		}
		else
		{
			enabledGroup.setSelected(false);
		}
	}

	public void save(SearchSet searchSet)
	{
		searchSet.setVirtualisationPath(enabledGroup.isSelected() ? xpathField.getText() : null);
		visualiserConfigs.save(searchSet);
	}
}
