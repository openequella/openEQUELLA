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

package com.tle.admin.plugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import com.dytech.gui.ComponentHelper;
import com.tle.common.i18n.CurrentLocale;

public class HelpListener extends MouseAdapter implements ActionListener
{
	private final String help;
	private final String title;
	private final Component comp;

	public HelpListener(Component comp, String title, String help)
	{
		this.comp = comp;
		this.title = title;
		this.help = CurrentLocale.get(help);
	}

	private void showHelp()
	{
		JDialog dialog = ComponentHelper.createJDialog(comp);
		dialog.getContentPane().add(new HelpPanel(dialog, help));
		dialog.setTitle(CurrentLocale.get("com.tle.admin.plugin.helplistener.title", title)); //$NON-NLS-1$

		dialog.setSize(new Dimension(600, 400));
		ComponentHelper.centreOnScreen(dialog);

		dialog.setModal(true);
		dialog.setVisible(true);
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		showHelp();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		showHelp();
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		//
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		//
	}

	protected static class HelpPanel extends JPanel implements ActionListener
	{
		private static final long serialVersionUID = 1L;
		private JButton close;
		private JTextArea area;
		private final Dialog dialog;

		public HelpPanel(Dialog dialog, String help)
		{
			this.dialog = dialog;
			setup();
			area.setText(help);
			area.setCaretPosition(0);
		}

		private void setup()
		{
			area = new JTextArea();
			area.setEditable(false);
			area.setWrapStyleWord(true);
			area.setLineWrap(true);

			close = new JButton(CurrentLocale.get("com.tle.admin.plugin.helplistener.close")); //$NON-NLS-1$
			close.addActionListener(this);

			JScrollPane scroller = new JScrollPane(area);
			scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			setLayout(new BorderLayout());

			JPanel temp = new JPanel();
			temp.setLayout(new BorderLayout());
			temp.add(scroller);
			temp.setBorder(new EmptyBorder(5, 5, 0, 5));
			add(temp, BorderLayout.CENTER);

			temp = new JPanel();
			temp.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			temp.add(close);
			add(temp, BorderLayout.SOUTH);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			dialog.setVisible(false);
		}
	}
}