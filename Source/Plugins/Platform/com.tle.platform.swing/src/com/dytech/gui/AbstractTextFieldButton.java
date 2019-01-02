/*
 * Copyright 2019 Apereo
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

package com.dytech.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;

public abstract class AbstractTextFieldButton extends JComponent
{
	protected JTextField field;
	protected JButton button;

	public AbstractTextFieldButton(String buttonText)
	{
		setupGui(buttonText);
		setupListeners();
	}

	protected abstract void buttonSelected();

	private void setupGui(String buttonText)
	{
		button = new JButton(buttonText);
		field = new JTextField();

		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.add(field);
		this.add(Box.createHorizontalStrut(5));
		this.add(button);
	}

	private void setupListeners()
	{
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				buttonSelected();
			}
		});
	}

	public void setButtonText(String text)
	{
		button.setText(text);
	}

	protected String getFieldText()
	{
		return field.getText();
	}

	protected void setFieldText(String s)
	{
		field.setText(s);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		button.setEnabled(enabled);
		field.setEnabled(enabled);
	}
}
