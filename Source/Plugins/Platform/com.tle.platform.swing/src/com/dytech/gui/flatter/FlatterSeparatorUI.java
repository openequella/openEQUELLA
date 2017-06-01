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

package com.dytech.gui.flatter;

import java.awt.Dimension;
import java.awt.Graphics;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSeparatorUI;

public class FlatterSeparatorUI extends BasicSeparatorUI implements Serializable
{
	private static final FlatterSeparatorUI m_separatorUI = new FlatterSeparatorUI();

	public FlatterSeparatorUI()
	{
		// Nothing to do here.
	}

	public static ComponentUI createUI(JComponent c)
	{
		return m_separatorUI;
	}

	@Override
	public void paint(Graphics g, JComponent c)
	{
		JSeparator sep = (JSeparator) c;
		Dimension dim = sep.getSize();

		g.setColor(sep.getForeground());

		if( sep.getOrientation() == SwingConstants.VERTICAL )
		{
			int x = dim.width / 2;
			g.drawLine(x, 0, x, dim.height);
		}
		else
		{
			int y = dim.height / 2;
			g.drawLine(0, y, dim.width, y);
		}
	}

	@Override
	public Dimension getPreferredSize(JComponent c)
	{
		if( ((JSeparator) c).getOrientation() == SwingConstants.VERTICAL )
		{
			return new Dimension(1, 0);
		}
		else
		{
			return new Dimension(0, 1);
		}
	}
}