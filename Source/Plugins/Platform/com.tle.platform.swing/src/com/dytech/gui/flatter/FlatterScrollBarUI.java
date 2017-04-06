package com.dytech.gui.flatter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class FlatterScrollBarUI extends BasicScrollBarUI implements Serializable
{
	protected Color mArrow = null;
	protected Color mTrack = null;
	protected Color mThumb = null;

	public FlatterScrollBarUI()
	{
		// Nothing to do here
	}

	public static ComponentUI createUI(JComponent c)
	{
		return new FlatterScrollBarUI();
	}

	@SuppressWarnings("nls")
	@Override
	public void installUI(JComponent c)
	{
		super.installUI(c);

		mTrack = UIManager.getColor("ScrollBar.track");
		mThumb = UIManager.getColor("ScrollBar.thumb");
		mArrow = UIManager.getColor("ScrollBar.arrow");
	}

	@Override
	protected void paintThumb(Graphics g, JComponent c, Rectangle bounds)
	{
		g.setColor(mThumb);
		g.fillRect(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4);
	}

	@Override
	protected void paintTrack(Graphics g, JComponent c, Rectangle bounds)
	{
		g.setColor(mTrack);
		g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	@Override
	protected JButton createIncreaseButton(int orientation)
	{
		return new FlatterIcons.ArrowButton(orientation);
	}

	@Override
	protected JButton createDecreaseButton(int orientation)
	{
		return new FlatterIcons.ArrowButton(orientation);
	}
}
