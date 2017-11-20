package com.tle.web.filemanager.applet.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.tle.common.i18n.CurrentLocale;
import com.tle.web.filemanager.common.FileInfo;

public abstract class AddressBarButton extends JLabel
{
	private boolean mouseOver;
	private FileInfo buttonFileInfo;

	public AddressBarButton(String label, String icon, FileInfo info)
	{
		super(label);

		setButtonFileInfo(info);
		setIcon(new ImageIcon(AddressBarButton.class.getResource(icon)));
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setAlignmentY(0.5f);
		// setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if( isEnabled() )
				{
					onClick();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				if( isEnabled() )
				{
					mouseOver = true;
					AddressBarButton.this.repaint();
				}
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				if( isEnabled() )
				{
					mouseOver = false;
					AddressBarButton.this.repaint();
				}
			}
		});
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		if( !enabled )
		{
			mouseOver = false;
		}
		super.setEnabled(enabled);
	}

	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		if( mouseOver )
		{
			Color underline = getForeground();

			// really all this size stuff below only needs to be recalculated if
			// font or text changes
			Rectangle2D textBounds = getFontMetrics(getFont()).getStringBounds(getText(), g);

			// this layout stuff assumes the icon is to the left, or null
			int y = getHeight() / 2 + (int) (textBounds.getHeight() / 2);
			int w = (int) textBounds.getWidth();
			int x = ((getIcon() == null || CurrentLocale.isRightToLeft()) ? 0 : getIcon().getIconWidth()
				+ getIconTextGap());

			g.setColor(underline);
			g.drawLine(x, y, x + w, y);
		}
	}

	public abstract void onClick();

	public FileInfo getButtonFileInfo()
	{
		return buttonFileInfo;
	}

	public void setButtonFileInfo(FileInfo buttonFileInfo)
	{
		this.buttonFileInfo = buttonFileInfo;
	}
}
