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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.plaf.basic.BasicArrowButton;

public abstract class FlatterIcons {
  public static class BoxIcon implements Icon {
    protected int width;
    protected int height;
    protected Color enabled;
    protected Color disabled;

    public BoxIcon(int width, int height, Color enabled, Color disabled) {
      this.width = width;
      this.height = height;
      this.enabled = enabled;
      this.disabled = disabled;
    }

    public void setColor(Component c, Graphics g) {
      g.setColor(c.isEnabled() ? enabled : disabled);
    }

    @Override
    public int getIconWidth() {
      return width;
    }

    @Override
    public int getIconHeight() {
      return height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      setColor(c, g);
      g.drawRoundRect(x, y, width, height, 2, 2);

      if (((AbstractButton) c).getModel().isSelected()) {
        g.fillRoundRect(x + 2, y + 2, width - 3, height - 3, 1, 1);
      }
    }
  }

  public static class BoxChecked extends BoxIcon {
    public BoxChecked(int width, int height, Color enabled, Color disabled) {
      super(width, height, enabled, disabled);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      setColor(c, g);
      g.drawRoundRect(x, y, width, height, 2, 2);
      g.fillRoundRect(x + 2, y + 2, width - 3, height - 3, 1, 1);
    }
  }

  public static class BoxUnchecked extends BoxIcon {
    public BoxUnchecked(int width, int height, Color enabled, Color disabled) {
      super(width, height, enabled, disabled);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      setColor(c, g);
      g.drawRoundRect(x, y, width, height, 2, 2);
    }
  }

  /**
   * Used to extend JButton which led to strange behaviour when clicking on it from JComboBox.
   *
   * @dytech.jira see Jira Defect TLE-1078 : http://apps.dytech.com.au/jira/browse/TLE-1078
   */
  public static class ArrowButton extends BasicArrowButton {
    public ArrowButton(int direction) {
      super(direction);

      setMargin(new Insets(0, 0, 0, 0));
      setBorder(null);
    }

    @Override
    public Dimension getMinimumSize() {
      return new Dimension(15, 15);
    }

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(15, 15);
    }

    @Override
    public void paint(Graphics g) {
      if (!isVisible()) {
        return;
      }
      if (isEnabled()) {
        g.setColor(FlatterDefaults.ScrollBar.Track);
      } else {
        g.setColor(FlatterDefaults.LightGray);
      }
      g.fillRect(0, 0, getWidth(), getHeight());

      if (isEnabled()) {
        g.setColor(FlatterDefaults.ScrollBar.Arrow);
      } else {
        g.setColor(FlatterDefaults.White);
      }
      Point centre = new Point(getWidth() / 2, getHeight() / 2);

      int arrowWidth = getWidth() / 3;
      int arrowOffset = arrowWidth / 2;

      switch (direction) {
        case NORTH:
          centre.y -= arrowOffset;
          for (int i = 0; i < arrowWidth; i++) {
            g.drawLine(centre.x - i, centre.y + i, centre.x + i, centre.y + i);
          }
          break;

        case SOUTH:
          centre.y += arrowOffset;
          for (int i = 0; i < arrowWidth; i++) {
            g.drawLine(centre.x - i, centre.y - i, centre.x + i, centre.y - i);
          }
          break;

        case WEST:
          centre.x -= arrowOffset;
          for (int i = 0; i < arrowWidth; i++) {
            g.drawLine(centre.x + i, centre.y - i, centre.x + i, centre.y + i);
          }
          break;

        case EAST:
          centre.x += arrowOffset;
          for (int i = 0; i < arrowWidth; i++) {
            g.drawLine(centre.x - i, centre.y - i, centre.x - i, centre.y + i);
          }
          break;
        default:
          break;
      }
    }
  }
}
