package com.dytech.gui.flatter;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.View;

public class FlatterTabbedPaneUI extends BasicTabbedPaneUI implements Serializable
{
	private Color selected, deselected;

	/** Creates a new instance of JFlatTabbedPaneUI */
	public FlatterTabbedPaneUI()
	{
		super();
		selected = FlatterDefaults.selectedTabColour1;
		deselected = FlatterDefaults.unselectedTabColour1;
	}

	public FlatterTabbedPaneUI(Color selected, Color deselected)
	{
		super();
		this.selected = selected;
		this.deselected = deselected;
	}

	public static ComponentUI createUI(JComponent c)
	{
		return new FlatterTabbedPaneUI();
	}

	@Override
	protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
		boolean isSelected)
	{
		// Set the colour according to selection:
		g.setColor(isSelected ? selected : deselected);

		// Trim the width and height:
		if( tabPlacement == TOP || tabPlacement == BOTTOM )
		{
			x += 1;
			w -= 2;

			h -= 1;
			if( tabPlacement == TOP )
			{
				y += 1;
			}
		}
		else if( tabPlacement == LEFT || tabPlacement == RIGHT )
		{
			y += 1;
			h -= 2;

			w -= 1;
			if( tabPlacement == LEFT )
			{
				x += 1;
			}
		}

		// Shift the tabs to leave a white space:
		if( tabPlacement == TOP )
		{
			y += 2;
			h -= 2;
		}
		else if( tabPlacement == BOTTOM )
		{
			y -= 2;
			h += 2;
		}
		else if( tabPlacement == LEFT )
		{
			x += 2;
			w -= 2;
		}
		else if( tabPlacement == RIGHT )
		{
			x -= 2;
			w += 2;
		}

		g.fillRect(x, y, w, h);
	}

	@Override
	protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title,
		Rectangle textRect, boolean isSelected)
	{
		g.setFont(font);

		View v = getTextViewForTab(tabIndex);
		if( v != null )
		{
			// html
			v.paint(g, textRect);
		}
		else
		{
			// plain text
			int mnemIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);

			if( tabPane.isEnabled() && tabPane.isEnabledAt(tabIndex) )
			{
				g.setColor(isSelected ? Color.white : Color.black);
				BasicGraphicsUtils.drawStringUnderlineCharAt(g, title, mnemIndex, textRect.x,
					textRect.y + metrics.getAscent());
			}
			else
			{ // tab disabled
				g.setColor(tabPane.getBackgroundAt(tabIndex).brighter());
				BasicGraphicsUtils.drawStringUnderlineCharAt(g, title, mnemIndex, textRect.x,
					textRect.y + metrics.getAscent());
				g.setColor(tabPane.getBackgroundAt(tabIndex).darker());
				BasicGraphicsUtils.drawStringUnderlineCharAt(g, title, mnemIndex, textRect.x,
					textRect.y + metrics.getAscent());
			}
		}
	}

	@Override
	protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected)
	{
		if( tabPlacement == LEFT )
		{
			return 1;
		}
		else if( tabPlacement == RIGHT )
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}

	@Override
	protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected)
	{
		if( tabPlacement == TOP )
		{
			return 2;
		}
		else if( tabPlacement == BOTTOM )
		{
			return -2;
		}
		else
		{
			return 0;
		}
	}

	@Override
	protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w,
		int h)
	{
		// Nothing here
	}

	@Override
	protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w,
		int h)
	{
		/*
		 * if(!paintBorder) return; g.setColor(BLUE); g.drawLine(x+1, y+2, x+1,
		 * h-3);
		 */}

	@Override
	protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w,
		int h)
	{
		/*
		 * if(!paintBorder) return; g.setColor(BLUE); g.drawLine(w-3, y+2, w-3,
		 * h-3);
		 */}

	@Override
	protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h)
	{
		/*
		 * if(!paintBorder) return; g.setColor(BLUE); g.drawLine(x+1, y+1, w-3,
		 * y+1);
		 */}

	@Override
	protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
		boolean isSelected)
	{
		// We don't want any borders!
	}

	@Override
	protected Insets getContentBorderInsets(int tabPlacement)
	{
		return new Insets(0, 0, 0, 0);
	}

	@Override
	protected Insets getSelectedTabPadInsets(int tabPlacement)
	{
		return new Insets(0, 0, 0, 0);
	}

	/**
	 * This doesn't change the tab size but adds padding around all the tabs.
	 */
	@Override
	protected Insets getTabAreaInsets(int tabPlacement)
	{
		switch( tabPlacement )
		{
			case TOP:
				return new Insets(2, 0, 0, 0);
			case BOTTOM:
				return new Insets(0, 0, 2, 0);
			case LEFT:
				return new Insets(0, 2, 0, 0);
			case RIGHT:
				return new Insets(0, 0, 0, 2);
			default:
				return new Insets(0, 0, 0, 0);
		}
	}

	@Override
	protected Insets getTabInsets(int tabPlacement, int tabIndex)
	{
		switch( tabPlacement )
		{
			case TOP:
				return new Insets(2, 1, 0, 1);
			case BOTTOM:
				return new Insets(0, 1, 2, 1);
			case LEFT:
				return new Insets(1, 2, 1, 0);
			case RIGHT:
				return new Insets(1, 0, 1, 2);
			default:
				return new Insets(0, 0, 0, 0);
		}
	}

	@Override
	protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
		Rectangle iconRect, Rectangle textRect, boolean isSelected)
	{
		// We don't have a focus indicator to paint!
	}
}