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

package com.tle.admin.taxonomy.tool;

import java.awt.Component;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.java.plugin.registry.Extension;

import com.tle.admin.baseentity.BaseEntityEditor.AbstractDetailsTab;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.RadioButtonChoiceList;
import com.tle.admin.gui.i18n.I18nTextArea;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.SingleUserSelector;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.core.remoting.RemoteUserService;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class DetailsTab extends BaseEntityTab<Taxonomy> implements AbstractDetailsTab<Taxonomy>
{
	private JTextField uuid;
	private I18nTextField name;
	private I18nTextArea description;
	private SingleUserSelector owner;
	private RadioButtonChoiceList<Taxonomy, Extension> choices;

	@Override
	public void init(Component parent)
	{
		uuid = new JTextField();
		uuid.setEditable(false);

		name = new I18nTextField(BundleCache.getLanguages());
		description = new I18nTextArea(BundleCache.getLanguages());
		owner = new SingleUserSelector(clientService.getService(RemoteUserService.class));

		choices = new RadioButtonChoiceList<Taxonomy, Extension>()
		{
			@Override
			public String getChoiceId(Extension choice)
			{
				return choice.getId();
			}

			@Override
			public DataSourceChoice getChoicePanel(Extension choice)
			{
				DataSourceChoice dsc = (DataSourceChoice) pluginService.getBean(choice.getDeclaringPluginDescriptor(),
					choice.getParameter("configPanel").valueAsString());
				dsc.setDynamicTabService(dynamicTabService);
				dsc.setClientService(clientService);
				dsc.setPluginService(pluginService);
				dsc.setReadOnly(state.isReadonly());
				return dsc;
			}

			@Override
			public String getChoiceTitle(Extension choice)
			{
				return CurrentLocale.get(choice.getParameter("nameKey").valueAsString());
			}

			@Override
			public String getSavedChoiceId(Taxonomy taxonomy)
			{
				return taxonomy.getDataSourcePluginId();
			}

			@Override
			public void setSavedChoiceId(Taxonomy taxonomy, String choiceId)
			{
				taxonomy.setDataSourcePluginId(choiceId);
			}
		};
		choices.loadChoices(pluginService.getConnectedExtensions("com.tle.admin.taxonomy.tool", "dataSourceChoice"));

		setLayout(new MigLayout("wrap 2,fillx", "[align label][fill, grow]25%"));

		add(new JLabel(s("uuid")));
		add(uuid);

		add(new JLabel(s("name")));
		add(name);

		add(new JLabel(s("desc")), "top");
		add(description, "height pref*3");

		add(new JLabel(s("owner")));
		add(owner);

		add(new JLabel(s("whichsource")), "span 2");
		add(choices, "skip");

		// Make sure things are read-only.
		if( state.isReadonly() )
		{
			name.setEnabled(false);
			description.setEnabled(false);
			owner.setEnabled(false);
			choices.setEnabled(false);
		}
	}

	@Override
	public String getTitle()
	{
		return s("title");
	}

	@Override
	public void addNameListener(KeyListener listener)
	{
		name.addKeyListener(listener);
	}

	@Override
	public void load()
	{
		final Taxonomy taxonomy = state.getEntity();
		uuid.setText(taxonomy.getUuid());

		name.load(taxonomy.getName());
		description.load(taxonomy.getDescription());
		owner.setUserId(taxonomy.getOwner());
		choices.load(taxonomy);
	}

	@Override
	public void save()
	{
		final Taxonomy taxonomy = state.getEntity();
		taxonomy.setName(name.save());
		taxonomy.setDescription(description.save());
		taxonomy.setOwner(owner.getUser().getUniqueID());
		choices.save(taxonomy);
	}

	@Override
	public void afterSave()
	{
		DataSourceChoice currentSelection = (DataSourceChoice) choices.getSelectedPanel();
		if( currentSelection != null )
		{
			currentSelection.afterSave();
		}
	}

	@Override
	public void validation() throws EditorException
	{
		if( name.isCompletelyEmpty() )
		{
			throw new EditorException(s("supplyname"));
		}

		if( owner.getUser() == null )
		{
			throw new EditorException(s("noowner"));
		}

		if( choices.isSelectionEmpty() )
		{
			throw new EditorException(s("nodatasource"));
		}
	}

	private String s(String keyPart)
	{
		return CurrentLocale.get("com.tle.admin.taxonomy.tool.detailstab." + keyPart);
	}
}
