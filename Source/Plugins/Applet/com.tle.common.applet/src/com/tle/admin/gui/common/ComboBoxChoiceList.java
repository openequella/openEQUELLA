/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.admin.gui.common;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.tle.common.Format;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.AppletGuiUtils;

public abstract class ComboBoxChoiceList<STATE_TYPE, CHOICE_TYPE>
	extends
		AbstractChoiceList<STATE_TYPE, CHOICE_TYPE, NameValue>
{
	private final JComboBox<NameValue> comboBox;
	private final JPanel choiceSink;

	@SuppressWarnings("nls")
	public ComboBoxChoiceList(String labelText)
	{
		comboBox = new JComboBox<>();
		comboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				NameValue nv = (NameValue) e.getItem();
				if( nv != null )
				{
					if( e.getStateChange() == ItemEvent.SELECTED )
					{
						choiceSelected(nv);
					}
					else if( e.getStateChange() == ItemEvent.DESELECTED )
					{
						choiceDeselected(nv);
					}
				}
			}
		});

		choiceSink = new JPanel(new GridLayout(1, 1));

		setLayout(new MigLayout("insets 0, hidemode 3", "[][grow]"));
		if( labelText != null )
		{
			add(new JLabel(labelText));
		}
		add(comboBox, "cell 1 0");
		add(choiceSink, "cell 0 1, span");

		changeDetector.watch(comboBox);
	}

	@Override
	protected void removeAndIgnoreOldComponents()
	{
		comboBox.removeAllItems();
	}

	@Override
	protected void _loadChoices(Iterable<CHOICE_TYPE> choiceList)
	{
		final List<NameValue> nvs = new ArrayList<NameValue>();
		for( CHOICE_TYPE choice : choiceList )
		{
			final String choiceId = getChoiceId(choice);
			final NameValue nv = new NameValue(getChoiceTitle(choice), choiceId);
			final DynamicChoicePanel<STATE_TYPE> choicePanel = getChoicePanel(choice);

			choicePanel.setId(choiceId);

			nvs.add(nv);
			choices.put(nv, choicePanel);
		}

		Collections.sort(nvs, Format.NAME_VALUE_COMPARATOR);
		AppletGuiUtils.addItemsToJCombo(comboBox, nvs);
	}

	@Override
	protected void updateChoicePanels()
	{
		choiceSink.removeAll();
		if( comboBox.isEnabled() )
		{
			DynamicChoicePanel<STATE_TYPE> dcp = choices.get(comboBox.getSelectedItem());
			if( dcp != null )
			{
				choiceSink.add(dcp);
				choiceSink.setVisible(true);
				choiceSink.revalidate();
				return;
			}
		}
		choiceSink.setVisible(false);
	}

	@Override
	public boolean isSelectionEmpty()
	{
		return comboBox.getSelectedIndex() < 0;
	}

	@Override
	protected void setChoicesEnabled(boolean enabled)
	{
		comboBox.setEnabled(enabled);
	}

	@Override
	protected boolean isEnabledAndSelected(NameValue nv)
	{
		return comboBox.isEnabled() && Objects.equals(nv, comboBox.getSelectedItem());
	}

	@Override
	protected void setSelectedChoiceComponent(NameValue nv)
	{
		comboBox.setSelectedItem(nv);
	}
}
