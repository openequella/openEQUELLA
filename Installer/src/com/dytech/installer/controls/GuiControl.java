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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.Item;
import com.tle.common.Check;

public abstract class GuiControl
{
	protected List targets;
	protected Vector items;
	protected String title;
	protected String description;

	public GuiControl(PropBagEx controlBag)
	{
		items = new Vector();
		targets = new ArrayList();

		createControl(controlBag);
	}

	public abstract JComponent generateControl();

	public abstract String getSelection();

	public void generate(JPanel panel)
	{
		StringBuilder sb = new StringBuilder();

		if( !Check.isEmpty(title) )
		{
			sb.append("<html><b>");
			sb.append(title);
		}

		if( !Check.isEmpty(description) )
		{
			if( sb.length() > 0 )
			{
				sb.append("</b><br>");
			}
			else
			{
				sb.append("<html>");
			}
			sb.append(description);
		}

		JPanel all = new JPanel(new BorderLayout());
		all.add(new JLabel(sb.toString()), BorderLayout.NORTH);

		JComponent control = generateControl();
		if( control != null )
		{
			all.add(control, BorderLayout.CENTER);
		}

		panel.add(all);
	}

	public void saveToTargets(PropBagEx outputBag)
	{
		String value = getSelection();

		Iterator i = targets.iterator();
		while( i.hasNext() )
		{
			String target = (String) i.next();
			outputBag.setNode(target, value);
		}

	}

	public void loadControl(PropBagEx xml)
	{
		if( xml != null )
		{
			Iterator i = targets.iterator();
			while( i.hasNext() )
			{
				String target = (String) i.next();
				String value = xml.getNode(target);

				if( items.size() > 0 && value.length() > 0 )
				{
					Item item = (Item) items.get(0);
					item.setValue(value);
				}
			}
		}
	}

	protected void createControl(PropBagEx controlBag)
	{
		title = controlBag.getNode("title");
		description = controlBag.getNode("description");

		Iterator iter = controlBag.iterateValues("target");
		while( iter.hasNext() )
		{
			targets.add(iter.next());
		}

		iter = controlBag.iterator("items/item");
		while( iter.hasNext() )
		{
			PropBagEx itemXml = (PropBagEx) iter.next();

			String name = itemXml.getNode("@name");
			String value = itemXml.getNode("@value");
			String select = itemXml.getNode("@default");

			if( name.length() == 0 )
			{
				name = value;
			}
			else if( value.length() == 0 )
			{
				value = name;
			}

			boolean selected = select.equals("true");

			Item item = new Item(name, value, selected);
			items.add(item);
		}
	}
}