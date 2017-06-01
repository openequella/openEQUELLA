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

package com.tle.admin.taxonomy.tool.internal;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ComponentHelper;
import com.tle.admin.gui.common.actions.AddAction;
import com.tle.admin.gui.common.actions.CancelAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.Pair;
import com.tle.common.applet.gui.AppletGuiUtils.BetterGroup;
import com.tle.common.i18n.CurrentLocale;

@SuppressWarnings("nls")
public class NewDataEntryDialog extends JPanel
{
	private JDialog dialog;
	private DataEntry entry;

	public NewDataEntryDialog(final Map<String, Pair<String, String>> predefinedTermDataKeys,
		final Set<String> existingSelections)
	{
		final BetterGroup<JRadioButton, String> choices = new BetterGroup<JRadioButton, String>(true);
		final JTextField customData = new JTextField();

		final TLEAction ok = new AddAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String key = choices.getSelectedValue();

				if( key != null )
				{
					entry = new DataEntry(key, predefinedTermDataKeys.get(key).getFirst());
					dialog.dispose();
					return;
				}

				key = customData.getText().trim();
				if( key.length() == 0 )
				{
					JOptionPane.showMessageDialog(dialog, s("validation.empty"), s("validation.title"),
						JOptionPane.WARNING_MESSAGE);
					customData.requestFocus();
					return;
				}

				if( existingSelections.contains(key) )
				{
					JOptionPane.showMessageDialog(dialog, s("validation.inuse"), s("validation.title"),
						JOptionPane.WARNING_MESSAGE);
					customData.requestFocus();
					return;
				}

				entry = new DataEntry(key);
				dialog.dispose();
			}
		};

		final TLEAction cancelAction = new CancelAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dialog.dispose();
			}
		};

		setLayout(new MigLayout("wrap 1", "[350px,fill]"));
		add(new JLabel(s("instructions")));

		// Include predefined data keys
		boolean choiceSelected = false;
		for( Map.Entry<String, Pair<String, String>> ptdk : predefinedTermDataKeys.entrySet() )
		{
			final String key = ptdk.getKey();
			final JRadioButton rb = new JRadioButton(ptdk.getValue().getSecond());

			choices.addButton(rb, key);
			add(rb);

			// Disable choices that are already in use
			if( existingSelections.contains(key) )
			{
				rb.setEnabled(false);
			}
			else if( !choiceSelected )
			{
				rb.setSelected(true);
				choiceSelected = true;
			}
		}

		// Custom data key
		JRadioButton customChoice = new JRadioButton(s("custom"), !choiceSelected);
		choices.addButton(customChoice, null);
		add(customChoice);
		add(customData, "gapleft indent");

		add(new JButton(ok), "gaptop unrelated, split, alignx right, tag ok");
		add(new JButton(cancelAction), "tag cancel");

		customData.setEnabled(customChoice.isSelected());
		customChoice.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				customData.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
	}

	private String s(String keyPart)
	{
		return CurrentLocale.get("com.tle.admin.taxonomy.tool.internal.tab.termeditor.newdata." + keyPart);
	}

	public DataEntry showDialog(Component parent)
	{
		dialog = ComponentHelper.createJDialog(parent);
		dialog.setTitle(s("title"));
		dialog.setContentPane(this);
		dialog.setResizable(false);
		dialog.setModal(true);
		dialog.pack();

		ComponentHelper.centreOnScreen(dialog);

		dialog.setVisible(true);
		dialog = null;

		return entry;
	}
}
