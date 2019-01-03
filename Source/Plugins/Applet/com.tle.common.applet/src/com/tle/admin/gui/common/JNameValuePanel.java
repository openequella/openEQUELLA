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

package com.tle.admin.gui.common;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;

import com.dytech.gui.Changeable;
import com.dytech.gui.TableLayout;
import com.tle.common.i18n.CurrentLocale;

public class JNameValuePanel implements Changeable
{
	private static final int FILL = 1;
	public JChangeDetectorPanel panel;
	private List<Comp> components;

	public JNameValuePanel()
	{
		panel = new JChangeDetectorPanel();
		components = new ArrayList<Comp>();
	}

	public void addComponent(JComponent comp)
	{
		components.add(new Comp(comp));
	}

	public void setBorder(Border border)
	{
		panel.setBorder(border);
	}

	public void addFillComponent(JComponent comp)
	{
		components.add(new Comp(comp, FILL));
	}

	public void addNameAndComponent(String name, JComponent comp)
	{
		if( name == null )
		{
			throw new NullPointerException();
		}
		components.add(new Comp(name, comp));
	}

	/**
	 * Automatically gets language bundle version of 'name'.
	 */
	public void addTextAndComponent(String name, JComponent comp)
	{
		if( name == null )
		{
			throw new NullPointerException();
		}
		name = CurrentLocale.get(name);
		components.add(new Comp(name, comp));
	}

	public void setupGUI()
	{
		int size = components.size();
		int[] rows = new int[size];
		int[] cols = new int[]{0, TableLayout.FILL};

		TableLayout layout = new TableLayout(rows, cols, 5, 5);
		panel.setLayout(layout);

		int maxWidth = 0;
		int i = 0;
		for( Comp comp : components )
		{
			boolean hasName = comp.name != null;
			boolean hasComp = comp.comp != null;
			if( hasName )
			{
				JLabel label = new JLabel(comp.name);
				if( hasComp )
				{
					maxWidth = Math.max(label.getPreferredSize().width, maxWidth);
					label.setLabelFor(comp.comp);
				}
				panel.add(label, new Rectangle(0, i, hasComp ? 1 : 2, 1));
			}

			if( hasComp )
			{
				panel.add(comp.comp, new Rectangle(hasName ? 1 : 0, i, hasName ? 1 : 2, 1));
			}

			if( comp.constraint == FILL )
			{
				layout.setRowSize(i, TableLayout.FILL);
			}
			else
			{
				layout.setRowSize(i, TableLayout.PREFERRED);
			}

			i++;
		}

		layout.setColumnSize(0, maxWidth);
	}

	public JChangeDetectorPanel getComponent()
	{
		setupGUI();
		return panel;
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return getComponent().hasDetectedChanges();
	}

	@Override
	public void clearChanges()
	{
		getComponent().clearChanges();
	}

	private static class Comp
	{
		JComponent comp;
		int constraint;
		String name;

		public Comp(JComponent comp)
		{
			this.comp = comp;
		}

		public Comp(JComponent comp, int constraint)
		{
			this.comp = comp;
			this.constraint = constraint;
		}

		public Comp(String name, JComponent comp)
		{
			this.name = name;
			this.comp = comp;
		}
	}
}
