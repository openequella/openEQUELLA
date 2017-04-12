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