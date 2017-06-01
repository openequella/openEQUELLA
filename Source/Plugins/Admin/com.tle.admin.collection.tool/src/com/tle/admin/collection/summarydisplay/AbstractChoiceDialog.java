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

package com.tle.admin.collection.summarydisplay;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ComponentHelper;
import com.tle.admin.gui.common.actions.AddAction;
import com.tle.admin.gui.common.actions.CancelAction;
import com.tle.common.applet.gui.AppletGuiUtils.BetterGroup;

@SuppressWarnings("nls")
public abstract class AbstractChoiceDialog<T> extends JPanel
{
	protected final BetterGroup<JRadioButton, String> choices;
	private final String dialogTitleText;
	private final boolean doubleClickToSelect;

	protected JDialog dialog;
	protected T selection;

	public AbstractChoiceDialog(String instructionText, String dialogTitleText)
	{
		this(instructionText, dialogTitleText, true);
	}

	public AbstractChoiceDialog(String instructionText, String dialogTitleText, boolean doubleClickToSelect)
	{
		this.dialogTitleText = dialogTitleText;
		this.doubleClickToSelect = doubleClickToSelect;
		this.choices = new BetterGroup<JRadioButton, String>(true);

		setLayout(new MigLayout("wrap 1", "[350px,fill]"));
		add(new JLabel(instructionText));

		add(new JButton(new AddAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				addClicked(choices.getSelectedValue());
			}
		}), "gaptop unrelated, split, alignx right, tag ok");

		add(new JButton(new CancelAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dialog.dispose();
			}
		}), "tag cancel");
	}

	public void addChoice(final String key, String displayText)
	{
		final JRadioButton rb = new JRadioButton(displayText);
		if( doubleClickToSelect )
		{
			rb.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					if( e.getClickCount() >= 2 )
					{
						addClicked(key);
					}
				}
			});
		}

		choices.addButton(rb, key);

		// Add after the instructions label and any existing choices
		add(rb, choices.size());
	}

	public void setChoicesEnabled(final Set<String> keys, boolean enabled)
	{
		for( String key : keys )
		{
			choices.setEnabledByValue(key, enabled);
		}
		ensureSelection();
	}

	private void ensureSelection()
	{
		JRadioButton cb = choices.getSelectedButton();
		if( cb == null || !cb.isEnabled() )
		{
			choices.selectFirstEnabledButton();
		}
	}

	protected abstract void addClicked(String selectedValue);

	public T showDialog(Component parent)
	{
		ensureSelection();

		dialog = ComponentHelper.createJDialog(parent);
		dialog.setTitle(dialogTitleText);
		dialog.setContentPane(this);
		dialog.setResizable(false);
		dialog.setModal(true);
		dialog.pack();

		ComponentHelper.centreOnScreen(dialog);

		dialog.setVisible(true);
		dialog = null;

		return selection;
	}
}
