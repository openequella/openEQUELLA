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

import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;

public class GLink extends GuiControl
{
	private String href;

	public GLink(PropBagEx controlBag) throws InstallerException
	{
		super(controlBag);
	}

	@Override
	protected void createControl(PropBagEx controlBag)
	{
		title = controlBag.getNode("title");
		href = controlBag.getNode("href");
	}

	@Override
	public String getSelection()
	{
		return new String();
	}

	@Override
	public void generate(JPanel panel)
	{
		final JLabel label;
		if( isBrowsingSupported() )
		{
			label = new JLabel("<html><a href=\"" + href + "\">" + title + "</a>");
			label.addMouseListener(new ClickHandler());
		}
		else
		{
			label = new JLabel(title);
		}
		panel.add(label);
	}

	@Override
	public JComponent generateControl()
	{
		return new JPanel();
	}

	private static boolean isBrowsingSupported()
	{
		if( !Desktop.isDesktopSupported() )
		{
			return false;
		}
		final Desktop desktop = java.awt.Desktop.getDesktop();
		if( desktop.isSupported(Desktop.Action.BROWSE) )
		{
			return true;
		}
		return false;
	}

	public class ClickHandler extends MouseAdapter
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			try
			{
				Desktop desktop = java.awt.Desktop.getDesktop();
				URI uri = new java.net.URI(href);
				desktop.browse(uri);
			}
			catch( Exception ex )
			{
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, "Unable to open link in the system browser",
					"Could not follow link", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
