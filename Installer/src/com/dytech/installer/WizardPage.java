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

package com.dytech.installer;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.dytech.devlib.PropBagEx;
import com.dytech.gui.VerticalFlowLayout;
import com.dytech.installer.controls.GCheckBoxGroup;
import com.dytech.installer.controls.GDirectorySelector;
import com.dytech.installer.controls.GEditBox;
import com.dytech.installer.controls.GFileSelector;
import com.dytech.installer.controls.GHeading;
import com.dytech.installer.controls.GHtml;
import com.dytech.installer.controls.GLink;
import com.dytech.installer.controls.GListBox;
import com.dytech.installer.controls.GParagraph;
import com.dytech.installer.controls.GPassword;
import com.dytech.installer.controls.GRadioButtonGroup;
import com.dytech.installer.controls.GShuffleBox;
import com.dytech.installer.controls.GuiControl;
import com.dytech.installer.controls.HelpButton;
import com.dytech.installer.controls.HostEditor;
import com.dytech.installer.controls.OracleIdSelector;
import com.dytech.installer.gui.JPanelAA;

/**
 * Generates a page for the installer from the XML file.
 * 
 * @author Nick Read
 */
@SuppressWarnings("nls")
public class WizardPage
{
	protected Wizard parent;
	protected int pageNumber;
	protected Vector<GuiControl> controls;
	protected PropBagEx pageBag;
	protected boolean enabled;
	protected boolean submitted;
	protected boolean valid;
	protected String title;
	protected String description;

	public WizardPage(PropBagEx pageBag, Wizard parent)
	{
		this.pageBag = pageBag;
		this.parent = parent;

		controls = new Vector<GuiControl>();
	}

	public GuiControl controlFactory(PropBagEx controlBag) throws InstallerException
	{
		GuiControl control = null;

		String cclass = controlBag.getNode("@class");

		if( cclass.equalsIgnoreCase("editbox") )
		{
			control = new GEditBox(controlBag);
		}
		else if( cclass.equalsIgnoreCase("password") )
		{
			control = new GPassword(controlBag);
		}
		else if( cclass.equalsIgnoreCase("radiogroup") )
		{
			control = new GRadioButtonGroup(controlBag);
		}
		else if( cclass.equalsIgnoreCase("heading") )
		{
			control = new GHeading(controlBag);
		}
		else if( cclass.equalsIgnoreCase("checkboxgroup") )
		{
			control = new GCheckBoxGroup(controlBag);
		}
		else if( cclass.equalsIgnoreCase("listbox") )
		{
			control = new GListBox(controlBag);
		}
		else if( cclass.equalsIgnoreCase("paragraph") )
		{
			control = new GParagraph(controlBag);
		}
		else if( cclass.equalsIgnoreCase("html") )
		{
			control = new GHtml(controlBag);
		}
		else if( cclass.equalsIgnoreCase("fileselector") )
		{
			control = new GFileSelector(controlBag);
		}
		else if( cclass.equalsIgnoreCase("directoryselector") )
		{
			control = new GDirectorySelector(controlBag);
		}
		else if( cclass.equalsIgnoreCase("shufflebox") )
		{
			control = new GShuffleBox(controlBag);
		}
		else if( cclass.equalsIgnoreCase("helpbutton") )
		{
			control = new HelpButton(controlBag);
		}
		else if( cclass.equalsIgnoreCase("hosteditor") )
		{
			control = new HostEditor(controlBag);
		}
		else if( cclass.equalsIgnoreCase("oracleidselector") )
		{
			control = new OracleIdSelector(controlBag, parent);
		}
		else if( cclass.equalsIgnoreCase("link") )
		{
			control = new GLink(controlBag);
		}
		else
		{
			throw new InstallerException("Control Error: Unknown control class '" + cclass + "'");
		}

		return control;
	}

	public void createControls() throws InstallerException
	{
		controls.clear();

		Iterator<PropBagEx> iter = pageBag.iterator("controls/control");
		while( iter.hasNext() )
		{
			PropBagEx controlXml = iter.next();

			GuiControl control = controlFactory(controlXml);
			control.loadControl(parent.getDefaults());
			control.loadControl(parent.getOutputNow());
			controls.add(control);
		}
	}

	public void createPage(JPanel panel) throws InstallerException
	{
		JPanelAA all = new JPanelAA();
		all.setLayout(new VerticalFlowLayout());

		createControls();
		Iterator<GuiControl> i = controls.iterator();
		while( i.hasNext() )
		{
			i.next().generate(all);
		}

		panel.add(all);
	}

	public void createButtons(JPanel panel) throws InstallerException
	{
		JButton button = null;

		Iterator<PropBagEx> iter = pageBag.iterator("buttons/button");
		if( !iter.hasNext() )
		{
			throw new InstallerException("Page Exception: Page " + pageNumber
				+ " does not define any navigation buttons.");
		}

		while( iter.hasNext() )
		{
			PropBagEx butBag = iter.next();

			String label = butBag.getNode("@label");
			String callback = butBag.getNode("@callback");
			String icon = butBag.getNode("@icon");
			String align = butBag.getNode("@align");

			button = new JButton(label);
			button.addActionListener(new ButtonHandler(parent, callback));

			if( icon.length() > 0 )
			{
				button.setIcon(new ImageIcon(getClass().getResource(icon)));
				if( align.equals("right") )
				{
					button.setHorizontalTextPosition(SwingConstants.LEFT);
				}
			}
			panel.add(button);
		}
	}

	public void createNavigators(JPanel panel) throws InstallerException
	{
		JPanel all = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		createButtons(all);
		panel.add(all);
	}

	public void saveControls(PropBagEx outputBag)
	{
		Iterator<GuiControl> i = controls.iterator();
		while( i.hasNext() )
		{
			GuiControl c = i.next();
			c.saveToTargets(outputBag);
		}
	}

	public int getPageNumber()
	{
		return pageNumber;
	}

	public class ButtonHandler implements ActionListener
	{
		private final Wizard installer;
		private final String callback;

		public ButtonHandler(Wizard installer, String callback)
		{
			this.installer = installer;
			this.callback = callback;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				saveControls(installer.getOutputNow());

				Class<?> cclass = Class.forName(callback);
				Callback ins = (Callback) cclass.newInstance();
				ins.task(installer);
			}
			catch( ClassNotFoundException ex )
			{
				System.out.println("Could not load class " + callback);
				ex.printStackTrace();
				System.exit(1);
			}
			catch( Exception ex )
			{
				ex.printStackTrace();
			}
		}
	}
}
