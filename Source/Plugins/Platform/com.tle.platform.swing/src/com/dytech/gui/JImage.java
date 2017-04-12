package com.dytech.gui;

import java.awt.Dimension;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * A standard Swing component which displays an image.
 * 
 * @author Nicholas Read
 */
public class JImage extends JLabel
{
	protected int height;
	protected int width;

	public JImage(String filename)
	{
		ImageIcon image = new ImageIcon(filename);
		setup(image);
	}

	public JImage(ImageIcon image)
	{
		setup(image);
	}

	public JImage(URL url)
	{
		ImageIcon image = new ImageIcon(url);
		setup(image);
	}

	private void setup(ImageIcon image)
	{
		setBorder(null);
		setOpaque(false);

		image.setImageObserver(this);
		this.setIcon(image);

		height = image.getIconHeight();
		width = image.getIconWidth();
	}

	@Override
	public Dimension getPreferredSize()
	{
		int extraWidth = getInsets().left + getInsets().right;
		int extraHeight = getInsets().top + getInsets().bottom;
		return new Dimension(width + extraWidth, height + extraHeight);
	}
}