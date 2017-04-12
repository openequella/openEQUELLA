package com.dytech.installer.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * A JPanel which makes all it's child components have smooth, anti-aliased
 * text.
 * 
 * @author Nicholas Read
 */
public class JPanelAA extends JPanel
{
	public JPanelAA()
	{
		super();
	}

	public JPanelAA(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
	}

	public JPanelAA(LayoutManager layout)
	{
		super(layout);
	}

	public JPanelAA(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
	}

	@Override
	public void paint(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		super.paint(g2);
	}
}