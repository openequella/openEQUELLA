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

package com.dytech.gui.flatter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicCheckBoxUI;

public class FlatterCheckBoxUI extends BasicCheckBoxUI implements Serializable, MouseListener, KeyListener
{
	private static final FlatterCheckBoxUI m_buttonUI = new FlatterCheckBoxUI();

	protected Color mBackgroundNormal = null;
	protected Color mBackgroundPressed = null;
	protected Color mBackgroundActive = null;
	protected Color mTextNormal = null;
	protected Color mTextPressed = null;
	protected Color mTextActive = null;
	protected Color mTextDisabled = null;
	protected Icon mIconChecked = null;
	protected Icon mIconUnchecked = null;
	protected Icon mIconPressedChecked = null;
	protected Icon mIconPressedUnchecked = null;
	protected int mTextIconGap = -1;

	public FlatterCheckBoxUI()
	{
		// Nothing to do here.
	}

	public static ComponentUI createUI(JComponent c)
	{
		return m_buttonUI;
	}

	@Override
	public synchronized void installUI(JComponent c)
	{
		super.installUI(c);

		mBackgroundNormal = UIManager.getColor("CheckBox.background");
		mBackgroundPressed = UIManager.getColor("CheckBox.backgroundPressed");
		mBackgroundActive = UIManager.getColor("CheckBox.backgroundActive");
		mTextNormal = UIManager.getColor("CheckBox.textNormal");
		mTextPressed = UIManager.getColor("CheckBox.textPressed");
		mTextActive = UIManager.getColor("CheckBox.textActive");
		mTextDisabled = UIManager.getColor("CheckBox.textDisabled");
		mTextIconGap = UIManager.getInt("CheckBox.textIconGap");
		mIconChecked = UIManager.getIcon("CheckBox.iconChecked");
		mIconUnchecked = UIManager.getIcon("CheckBox.iconUnchecked");
		mIconPressedChecked = UIManager.getIcon("CheckBox.iconPressedChecked");
		mIconPressedUnchecked = UIManager.getIcon("CheckBox.iconPressedUnchecked");

		c.setBackground(mBackgroundNormal);
		c.addMouseListener(this);
	}

	@Override
	public void uninstallUI(JComponent c)
	{
		super.uninstallUI(c);
		c.removeMouseListener(this);
	}

	@Override
	public synchronized void paint(Graphics g, JComponent c)
	{
		AbstractButton b = (AbstractButton) c;
		ButtonModel model = b.getModel();
		Dimension d = b.getSize();

		g.setFont(c.getFont());
		FontMetrics fm = g.getFontMetrics();

		Icon icon = mIconUnchecked;
		if( model.isPressed() && model.isSelected() )
		{
			icon = mIconPressedChecked;
		}
		else if( model.isPressed() && !model.isSelected() )
		{
			icon = mIconPressedUnchecked;
		}
		else if( !model.isPressed() && model.isSelected() )
		{
			icon = mIconChecked;
		}

		int x = 0;
		int y = (d.height - icon.getIconHeight()) / 2;
		icon.paintIcon(c, g, x, y);

		if( b.isEnabled() )
		{
			g.setColor(mTextNormal);
		}
		else
		{
			g.setColor(mTextDisabled);
		}

		String caption = b.getText();
		x = icon.getIconWidth() + mTextIconGap;
		y = (d.height + fm.getAscent()) / 2;
		g.drawString(caption, x, y);
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		// We don't care about this event
	}

	@Override
	public synchronized void mousePressed(MouseEvent e)
	{
		JComponent c = (JComponent) e.getComponent();
		if( c.isEnabled() )
		{
			c.setForeground(mTextPressed);
			c.setBackground(mBackgroundPressed);
			c.repaint();
		}
	}

	@Override
	public synchronized void mouseReleased(MouseEvent e)
	{
		JComponent c = (JComponent) e.getComponent();
		c.setForeground(mTextNormal);
		c.setBackground(mBackgroundNormal);
		c.repaint();
	}

	@Override
	public synchronized void mouseEntered(MouseEvent e)
	{
		JComponent c = (JComponent) e.getComponent();
		if( c.isEnabled() )
		{
			c.setForeground(mTextActive);
			c.setBackground(mBackgroundActive);
			c.repaint();
		}
	}

	@Override
	public synchronized void mouseExited(MouseEvent e)
	{
		JComponent c = (JComponent) e.getComponent();
		c.setForeground(mTextNormal);
		c.setBackground(mBackgroundNormal);
		c.repaint();
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		// We don't care about this event.
	}

	@Override
	public synchronized void keyPressed(KeyEvent e)
	{
		int code = e.getKeyCode();
		if( code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE )
		{
			JComponent c = (JComponent) e.getComponent();
			c.setForeground(mTextPressed);
			c.setBackground(mBackgroundPressed);
		}
	}

	@Override
	public synchronized void keyReleased(KeyEvent e)
	{
		int code = e.getKeyCode();
		if( code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE )
		{
			JComponent c = (JComponent) e.getComponent();
			c.setForeground(mTextNormal);
			c.setBackground(mBackgroundNormal);
		}
	}
}
