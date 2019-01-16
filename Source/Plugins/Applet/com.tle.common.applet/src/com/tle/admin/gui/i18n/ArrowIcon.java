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

package com.tle.admin.gui.i18n;

import static javax.swing.SwingConstants.EAST;
import static javax.swing.SwingConstants.NORTH;
import static javax.swing.SwingConstants.SOUTH;
import static javax.swing.SwingConstants.WEST;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.UIManager;

/** Blatent rip-off of Sun's BasicArrowButton, but with text. */
public class ArrowIcon implements Icon {
  private static final int SIZE = 8;

  private final int direction;
  private final Color darkShadow;

  public ArrowIcon(int direction) {
    this.direction = direction;
    this.darkShadow = UIManager.getColor("controlDkShadow"); // $NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics,
   * int, int)
   */
  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    int drawSize = SIZE / 2;
    int offset = drawSize / 2;
    paintTriangle(g, x + offset, y + offset, drawSize);
  }

  /*
   * (non-Javadoc)
   * @see javax.swing.Icon#getIconHeight()
   */
  @Override
  public int getIconHeight() {
    return SIZE;
  }

  /*
   * (non-Javadoc)
   * @see javax.swing.Icon#getIconWidth()
   */
  @Override
  public int getIconWidth() {
    return SIZE;
  }

  private void paintTriangle(Graphics g, int x, int y, int size) {
    Color oldColor = g.getColor();
    int mid, i, j;

    j = 0;
    size = Math.max(size, 2);
    mid = (size / 2) - 1;

    g.translate(x, y);
    g.setColor(darkShadow);

    switch (direction) {
      case NORTH:
        for (i = 0; i < size; i++) {
          g.drawLine(mid - i, i, mid + i, i);
        }
        break;
      case SOUTH:
        j = 0;
        for (i = size - 1; i >= 0; i--) {
          g.drawLine(mid - i, j, mid + i, j);
          j++;
        }
        break;
      case WEST:
        for (i = 0; i < size; i++) {
          g.drawLine(i, mid - i, i, mid + i);
        }
        break;
      case EAST:
        j = 0;
        for (i = size - 1; i >= 0; i--) {
          g.drawLine(j, mid - i, j, mid + i);
          j++;
        }
        break;
      default:
        break;
    }
    g.translate(-x, -y);
    g.setColor(oldColor);
  }
}
