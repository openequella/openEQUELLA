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

package com.dytech.installer.controls;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;
import com.dytech.installer.Item;

public abstract class GResourceSelector extends GuiControl implements ActionListener
{
	protected JTextField field;

	public GResourceSelector(PropBagEx controlBag) throws InstallerException
	{
		super(controlBag);
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.controls.GuiControl#getSelection()
	 */
	@Override
	public String getSelection()
	{
		return field.getText();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.controls.GuiControl#generateControl()
	 */
	@Override
	public JComponent generateControl()
	{
		field = new JTextField();
		field.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));

		if( items.size() >= 1 )
		{
			field.setText(((Item) items.get(0)).getValue());
		}

		JButton browse = new JButton("Browse");
		browse.setIcon(new ImageIcon(getClass().getResource("/images/browse.gif")));
		browse.setHorizontalTextPosition(SwingConstants.RIGHT);
		Dimension browseSize = browse.getPreferredSize();
		browseSize.height = 20;
		browse.setMaximumSize(browseSize);
		browse.addActionListener(this);

		JPanel group = new JPanel();
		group.setLayout(new BoxLayout(group, BoxLayout.X_AXIS));
		group.add(field);
		group.add(Box.createRigidArea(new Dimension(5, 0)));
		group.add(browse);
		group.setAlignmentX(Component.LEFT_ALIGNMENT);

		return group;
	}
}
