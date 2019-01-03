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

package com.dytech.gui;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.border.EmptyBorder;

public class JLinkButton extends JButton
{
	/**
	 * Creates a button with no set text or icon.
	 */
	public JLinkButton()
	{
		super();
	}

	/**
	 * Creates a button with an icon.
	 */
	public JLinkButton(Icon icon)
	{
		super(icon);
	}

	/**
	 * Creates a button with text.
	 */
	public JLinkButton(String text)
	{
		super();
		init(text);
	}

	/**
	 * Creates a button with initial text and an icon.
	 */
	public JLinkButton(String text, Icon icon)
	{
		super(icon);
		init(text);
	}

	@Override
	public void setEnabled(boolean b)
	{
		super.setEnabled(b);

		if( b )
		{
			setForeground(Color.blue);
		}
		else
		{
			setForeground(Color.gray);
		}

		updateUI();
	}

	private void init(String text)
	{
		setText("<html><u>" + text + "</u><html>");
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setBorder(new EmptyBorder(3, 3, 3, 3));
		setForeground(Color.blue);
		setOpaque(false);
		setContentAreaFilled(false);
	}

	public static void main(String args[])
	{
		JDialog d = new JDialog();
		d.setSize(300, 300);
		d.setModal(true);

		JLinkButton a = new JLinkButton("Hello", new ImageIcon("c:\\image.gif"));
		JLinkButton b = new JLinkButton("Disabled");
		b.setEnabled(false);

		d.getContentPane().add(a);

		d.setVisible(true);
		System.exit(0);
	}
}