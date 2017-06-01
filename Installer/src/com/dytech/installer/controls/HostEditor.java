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

package com.dytech.installer.controls;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.dytech.devlib.PropBagEx;
import com.dytech.gui.TableLayout;
import com.dytech.installer.InstallerException;
import com.dytech.installer.Item;

public class HostEditor extends GuiControl
{
	private static final String[] LOCALHOST = {"localhost", "127.0.0.1"};
	private static final String DEFAULT_LOCALHOST = LOCALHOST[0];

	private JTextField field;
	private JRadioButton localhost;
	private JRadioButton remotehost;

	public HostEditor(PropBagEx controlBag) throws InstallerException
	{
		super(controlBag);
	}

	@Override
	public String getSelection()
	{
		if( localhost.isSelected() )
		{
			return DEFAULT_LOCALHOST;
		}
		else
		{
			return field.getText().trim();
		}
	}

	@Override
	public JComponent generateControl()
	{
		localhost = new JRadioButton("This machine", true);
		remotehost = new JRadioButton("A different server");
		field = new JTextField();

		ButtonGroup group = new ButtonGroup();
		group.add(localhost);
		group.add(remotehost);

		ActionListener buttonListener = new ActionListener()
		{
			/*
			 * (non-Javadoc)
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e)
			{
				update();
			}
		};

		localhost.addActionListener(buttonListener);
		remotehost.addActionListener(buttonListener);

		final int height1 = field.getPreferredSize().height;
		final int width1 = remotehost.getPreferredSize().width;
		final int[] rows = {height1, height1,};
		final int[] cols = {width1, TableLayout.FILL};

		JPanel all = new JPanel(new TableLayout(rows, cols));
		all.add(localhost, new Rectangle(0, 0, 2, 1));
		all.add(remotehost, new Rectangle(0, 1, 1, 1));
		all.add(field, new Rectangle(1, 1, 1, 1));

		if( items.size() >= 1 )
		{
			String host = ((Item) items.get(0)).getValue();
			if( host.length() == 0 || isLocalhost(host) )
			{
				localhost.setSelected(true);
			}
			else
			{
				remotehost.setSelected(true);
				field.setText(host);
			}
		}

		update();

		return all;
	}

	private boolean isLocalhost(String host)
	{
		for( int i = 0; i < LOCALHOST.length; i++ )
		{
			if( host.equals(LOCALHOST[i]) )
			{
				return true;
			}
		}
		return false;
	}

	private void update()
	{
		field.setEnabled(remotehost.isSelected());
	}
}
