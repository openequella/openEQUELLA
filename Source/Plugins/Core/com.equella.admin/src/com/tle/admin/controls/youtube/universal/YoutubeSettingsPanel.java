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

package com.tle.admin.controls.youtube.universal;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import com.dytech.edge.admin.wizard.DualShuffleList;
import com.dytech.edge.wizard.beans.control.WizardControlItem;
import com.tle.admin.controls.universal.UniversalControlSettingPanel;
import com.tle.admin.helper.GroupBox;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.wizard.controls.universal.UniversalSettings;
import com.tle.common.wizard.controls.youtube.YoutubeSettings;

/**
 * @author Peng
 */
public class YoutubeSettingsPanel extends UniversalControlSettingPanel
{
	private static final long serialVersionUID = 1L;

	private JRadioButton allow;
	private JRadioButton restrict;

	private GroupBox enableChannel;
	private JLabel displayLabel;
	private JLabel helpLabel1;
	private JLabel helpLabel2;
	private DualShuffleList channels;

	public YoutubeSettingsPanel()
	{
		super();
		createGUI();
	}

	@SuppressWarnings("nls")
	private void createGUI()
	{
		enableChannel = GroupBox.withCheckBox(getString("allowChannel"), false);
		setLayout(new MigLayout("wrap, insets 0", "[grow]", "[][grow]"));

		add(enableChannel, "grow");

		enableChannel.getInnerPanel().setLayout(new MigLayout("wrap", "[grow]"));

		allow = new JRadioButton(getString("option.allowChannel"));
		restrict = new JRadioButton(getString("option.restrictChannel"));
		allow.setSelected(true);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(allow);
		buttonGroup.add(restrict);

		displayLabel = new JLabel(getString("allowChannel"));
		helpLabel1 = new JLabel(getString("channel.help1"));
		helpLabel2 = new JLabel(getString("channel.help2"));
		channels = new DualShuffleList(getString("label.channelName"),
			getString("label.userName"));
		channels.setEnabled(false);

		enableChannel.add(allow);
		enableChannel.add(restrict);
		enableChannel.add(displayLabel, "gaptop 10px");
		enableChannel.add(channels, "gap 10px, grow");
		enableChannel.add(helpLabel1, "gaptop 10px");
		enableChannel.add(helpLabel2, "gaptop 10px");
	}

	@SuppressWarnings("nls")
	@Override
	protected String getTitleKey()
	{
		return getKey("youtube.settings.title");
	}

	@Override
	public void load(UniversalSettings state)
	{
		final YoutubeSettings settings = new YoutubeSettings(state);
		if( settings.isAllowChannelSelection() )
		{
			enableChannel.setSelected(true);
			if( settings.isOptionAllowChannelSelection() )
			{
				allow.setSelected(true);
			}
			else if( settings.isOptionRestrictChannelSelection() )
			{
				restrict.setSelected(true);
			}
		}

		if( settings.getChannels() != null )
		{
			channels.clear();

			for( Pair<String, String> items : settings.getChannels() )
			{
				WizardControlItem channle = new WizardControlItem();
				channle.setValue(items.getFirst());
				channle.setName(LangUtils.createTextTempLangugageBundle(items.getSecond()));
				channels.addItem(channle);
			}
		}
	}

	@Override
	public void removeSavedState(UniversalSettings state)
	{
		// nfa
	}

	@Override
	public void save(UniversalSettings state)
	{
		List<Pair<String, String>> channel = new ArrayList<Pair<String, String>>();

		for( WizardControlItem item : channels.getItems() )
		{
			channel.add(new Pair<String, String>(item.getValue(), CurrentLocale.get(item.getName())));
		}

		final YoutubeSettings settings = new YoutubeSettings(state);
		settings.setAllowChannelSelection(enableChannel.isSelected());
		settings.setOptionAllowChannelSelection(allow.isSelected());
		settings.setOptionRestrictChannelSelection(restrict.isSelected());
		settings.setChannels(channel);

	}

}