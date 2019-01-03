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

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class JHoverButton extends JButton implements MouseListener
{
	public static final int HOVER_PLAIN = Font.PLAIN;
	public static final int HOVER_BOLD = Font.BOLD;
	public static final int HOVER_ITALIC = Font.ITALIC;

	private Font original = null;
	private int style = Font.BOLD;

	public JHoverButton()
	{
		super();
		setup(HOVER_BOLD);
	}

	public JHoverButton(Action a)
	{
		super(a);
		setup(HOVER_BOLD);
	}

	public JHoverButton(Icon icon)
	{
		super(icon);
		setup(HOVER_BOLD);
	}

	public JHoverButton(String text)
	{
		super(text);
		setup(HOVER_BOLD);
	}

	public JHoverButton(String text, Icon icon)
	{
		super(text, icon);
		setup(HOVER_BOLD);
	}

	public JHoverButton(String text, int style)
	{
		super(text);
		setup(style);
	}

	public JHoverButton(String text, Icon icon, int style)
	{
		super(text, icon);
		setup(style);
	}

	private void setup(int style)
	{
		this.style = style;
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addMouseListener(this);
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		if( original != null )
		{
			setFont(original);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		original = getFont();
		setFont(new Font(original.getName(), style, original.getSize()));
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		// We don't care about this event
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		// We don't care about this event
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		// We don't care about this event
	}

	public static void main(String[] args)
	{
		JHoverButton b1 = new JHoverButton("Enabled Example", JHoverButton.HOVER_ITALIC);

		JHoverButton b2 = new JHoverButton("Disabled Example", JHoverButton.HOVER_PLAIN);
		b2.setEnabled(false);

		JPanel p = new JPanel(new FlowLayout());
		p.add(b1);
		p.add(b2);

		JFrame f = new JFrame("JHoverButton Test");
		f.getContentPane().add(p);
		f.setBounds(300, 300, 300, 300);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}