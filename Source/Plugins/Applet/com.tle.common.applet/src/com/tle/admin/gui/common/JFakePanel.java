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

package com.tle.admin.gui.common;

import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.border.Border;

@Deprecated
public class JFakePanel
{
	protected JChangeDetectorPanel panel;

	public JFakePanel()
	{
		this.panel = new JChangeDetectorPanel();
	}

	public void add(Component comp)
	{
		panel.add(comp);
	}

	public void add(Component comp, Object constaint)
	{
		panel.add(comp, constaint);
	}

	public void setLayout(LayoutManager layout)
	{
		panel.setLayout(layout);
	}

	public void setBorder(Border border)
	{
		panel.setBorder(border);
	}

	public JPanel getComponent()
	{
		return panel;
	}
}
