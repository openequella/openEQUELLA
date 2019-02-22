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

package com.tle.admin.helper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * Class JFlatScrollBar File JFlatScrollBar.java Description: This is used in place of the standard
 * scroll bar in a JScrollPane. This implementation overwrites the paint method to show a scroll bar
 * without any 3d effects. Author: Ben Millar Date: 4/9/2002
 */
public class JFlatScrollBar extends JScrollBar {
  private static final long serialVersionUID = 1L;

  // Inner class JFlatArrowButton
  // Describes a button with an arrow on it pointing up, down, left or right.
  private static class JFlatArrowButton extends JButton {
    private static final long serialVersionUID = 1L;
    public static final int UP = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;

    private int direction;

    public JFlatArrowButton(int direction) {
      super();
      setBorder(null);
      setBackground(new Color(102, 102, 102));
      this.direction = direction;
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

      // Paint the background:
      Color bg = getBackground();
      g.setColor(bg);
      g.fillRect(0, 0, getWidth(), getHeight());

      g.setColor(Color.white);
      Point centre = new Point(getWidth() / 2, getHeight() / 2);

      switch (direction) {
        case UP:
          centre.y -= 2;
          for (int i = 0; i < 4; i++) {
            g.drawLine(centre.x - i, centre.y + i, centre.x + i, centre.y + i);
          }
          break;
        case DOWN:
          centre.y += 2;
          for (int i = 0; i < 4; i++) {
            g.drawLine(centre.x - i, centre.y - i, centre.x + i, centre.y - i);
          }
          break;
        case LEFT:
          centre.x -= 2;
          for (int i = 0; i < 4; i++) {
            g.drawLine(centre.x + i, centre.y - i, centre.x + i, centre.y + i);
          }
          break;
        case RIGHT:
          centre.x += 2;
          for (int i = 0; i < 4; i++) {
            g.drawLine(centre.x - i, centre.y - i, centre.x - i, centre.y + i);
          }
          break;
        default:
          break;
      }
    }
  }

  // Inner class FlatScrollBarUI describes how the look and
  // feel of the scroll bar should behave:
  private static class FlatScrollBarUI extends BasicScrollBarUI {
    public FlatScrollBarUI() {
      super();
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
      g.setColor(Color.white);
      g.fillRect(
          thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4);
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
      g.setColor(new Color(102, 102, 102));
      g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
      if (orientation == SOUTH) {
        // note that increase is along y-axis; ie. down is positive
        return new JFlatArrowButton(JFlatArrowButton.DOWN);
      } else {
        return new JFlatArrowButton(JFlatArrowButton.RIGHT);
      }
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
      if (orientation == NORTH) {
        // note that decrease is along y-axis; ie. up is negative
        return new JFlatArrowButton(JFlatArrowButton.UP);
      } else {
        return new JFlatArrowButton(JFlatArrowButton.LEFT);
      }
    }
  }

  public JFlatScrollBar(int orientation) {
    super(orientation);
    this.orientation = orientation;

    setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    setMinimumSize(new java.awt.Dimension(15, 15));
    setPreferredSize(new java.awt.Dimension(15, 15));
    setBackground(new Color(102, 102, 102));

    setUI(new FlatScrollBarUI());

    setUnitIncrement(15);
    setBlockIncrement(100);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(15, 15);
  }
}
