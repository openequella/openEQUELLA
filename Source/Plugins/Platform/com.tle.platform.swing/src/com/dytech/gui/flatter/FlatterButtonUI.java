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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

public class FlatterButtonUI extends BasicButtonUI implements Serializable, KeyListener {
  private static final FlatterButtonUI m_buttonUI = new FlatterButtonUI();

  protected Color mBackgroundNormal = null;
  protected Color mBackgroundPressed = null;
  protected Color mBackgroundActive = null;
  protected Color mTextNormal = null;
  protected Color mTextPressed = null;
  protected Color mTextActive = null;
  protected Color mTextDisabled = null;
  protected Border mBorder = null;

  public FlatterButtonUI() {
    // Nothing to do here.
  }

  public static ComponentUI createUI(JComponent c) {
    return m_buttonUI;
  }

  @Override
  public void installUI(JComponent c) {
    super.installUI(c);

    mBackgroundNormal = UIManager.getColor("Button.background");
    mBackgroundPressed = UIManager.getColor("Button.backgroundPressed");
    mBackgroundActive = UIManager.getColor("Button.backgroundActive");
    mTextNormal = UIManager.getColor("Button.textNormal");
    mTextPressed = UIManager.getColor("Button.textPressed");
    mTextActive = UIManager.getColor("Button.textActive");
    mTextDisabled = UIManager.getColor("Button.textDisabled");
    mBorder = UIManager.getBorder("Button.border");

    c.addKeyListener(this);
  }

  @Override
  public void uninstallUI(JComponent c) {
    c.removeKeyListener(this);
    super.uninstallUI(c);
  }

  @Override
  public void paint(Graphics g, JComponent c) {
    AbstractButton b = (AbstractButton) c;
    ButtonModel m = b.getModel();

    b.setBackground(m.isPressed() ? mBackgroundPressed : mBackgroundNormal);
    b.setForeground(m.isEnabled() ? mTextNormal : mTextDisabled);

    super.paint(g, c);
  }

  @Override
  protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect) {
    AbstractButton b = (AbstractButton) c;
    ButtonModel model = b.getModel();
    Icon icon = b.getIcon();
    Icon tmpIcon = null;

    if (icon == null) {
      return;
    }

    if (!model.isEnabled()) {
      if (model.isSelected()) {
        tmpIcon = b.getDisabledSelectedIcon();
      } else {
        tmpIcon = b.getDisabledIcon();
      }
    } else if (model.isPressed() && model.isArmed()) {
      tmpIcon = b.getPressedIcon();
      if (tmpIcon != null) {
        // revert back to 0 offset
        clearTextShiftOffset();
      }
    } else if (b.isRolloverEnabled() && model.isRollover()) {
      if (model.isSelected()) {
        tmpIcon = b.getRolloverSelectedIcon();
      } else {
        tmpIcon = b.getRolloverIcon();
      }
    } else if (model.isSelected()) {
      tmpIcon = b.getSelectedIcon();
    }

    if (tmpIcon != null) {
      icon = tmpIcon;
    }

    if (model.isPressed() && model.isArmed()) {
      icon.paintIcon(c, g, iconRect.x + getTextShiftOffset(), iconRect.y + getTextShiftOffset());
    } else {
      icon.paintIcon(c, g, iconRect.x, iconRect.y);
    }
  }

  @Override
  public Dimension getPreferredSize(JComponent c) {
    Dimension d = super.getPreferredSize(c);
    if (mBorder != null) {
      Insets ins = mBorder.getBorderInsets(c);
      d.setSize(d.width + ins.left + ins.right, d.height + ins.top + ins.bottom);
    }
    return d;
  }

  @Override
  public void keyTyped(KeyEvent e) {
    // We don't care about this event
  }

  @Override
  public void keyPressed(KeyEvent e) {
    int code = e.getKeyCode();
    if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
      JComponent c = (JComponent) e.getComponent();
      c.setForeground(mTextPressed);
      c.setBackground(mBackgroundPressed);
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
    int code = e.getKeyCode();
    if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
      JComponent c = (JComponent) e.getComponent();
      c.setForeground(mTextNormal);
      c.setBackground(mBackgroundNormal);
    }
  }
}
