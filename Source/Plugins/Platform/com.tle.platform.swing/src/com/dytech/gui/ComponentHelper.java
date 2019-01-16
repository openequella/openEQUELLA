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

package com.dytech.gui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

public final class ComponentHelper {
  private ComponentHelper() {
    throw new Error();
  }

  public static void ensureMinimumSize(Component c, Dimension d) {
    ensureMinimumSize(c, d.width, d.height);
  }

  public static void ensureMinimumSize(Component c, int width, int height) {
    Dimension size = c.getSize();

    if (size.width < width) {
      size.width = width;
    }

    if (size.height < height) {
      size.height = height;
    }

    c.setSize(size);
  }

  public static void ensureMaximumSize(Component c, Dimension d) {
    ensureMaximumSize(c, d.width, d.height);
  }

  public static void ensureMaximumSize(Component c, int width, int height) {
    Dimension size = c.getSize();

    if (size.width > width) {
      size.width = width;
    }

    if (size.height > height) {
      size.height = height;
    }

    c.setSize(size);
  }

  public static void centre(Component parent, Component child) {
    child.setBounds(centre(parent.getBounds(), child.getBounds()));
  }

  public static void centreOnScreen(Window window) {
    Window owner = window.getOwner();

    // If the window has an owner, use the same graphics configuration so it
    // will
    // open on the same screen. Otherwise, grab the mouse pointer and work
    // from there.
    GraphicsConfiguration gc =
        owner != null
            ? owner.getGraphicsConfiguration()
            : MouseInfo.getPointerInfo().getDevice().getDefaultConfiguration();

    if (gc != null) {
      window.setBounds(centre(getUsableScreenBounds(gc), window.getBounds()));
    } else {
      // Fall-back to letting Java do the work
      window.setLocationRelativeTo(null);
    }
  }

  public static Rectangle centre(Rectangle parent, Rectangle child) {
    child.x = (parent.width / 2) + parent.x - (child.width / 2);
    child.y = (parent.height / 2) + parent.y - (child.height / 2);

    return child;
  }

  /**
   * Calculates the dimensions of a given percentage of the screen.
   *
   * @param width percentage of screen width between 0 and 1.
   * @param height percentage of screen height between 0 and 1.
   * @return Size of the area for given screen percentage.
   * @deprecated Doesn't work in dual-monitor environments. Use percentageOfScreen(Window, float,
   *     float)
   */
  @Deprecated
  public static Dimension percentageOfScreen(float width, float height) {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

    screen.width *= width;
    screen.height *= height;

    return screen;
  }

  public static void percentageOfScreen(Window window, float width, float height) {
    Rectangle screen = getUsableScreenBounds(window.getGraphicsConfiguration());
    window.setSize((int) (screen.width * width), (int) (screen.height * height));
  }

  public static Rectangle getUsableScreenBounds(GraphicsConfiguration gc) {
    Rectangle screen = gc.getBounds();
    Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
    screen.x += insets.left;
    screen.y += insets.top;
    screen.width -= insets.left + insets.right;
    screen.height -= insets.top + insets.bottom;
    return screen;
  }

  public static GraphicsConfiguration getGraphicsConfigurationForPoint(Point l) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    for (GraphicsDevice gd : ge.getScreenDevices()) {
      GraphicsConfiguration gc = gd.getDefaultConfiguration();
      if (gc.getBounds().contains(l)) {
        return gc;
      }
    }
    return null;
  }

  public static JDialog createJDialog(Component parent) {
    Window window = SwingUtilities.getWindowAncestor(parent);
    if (window == null && parent instanceof Window) {
      window = (Window) parent;
    }

    if (window instanceof Frame) {
      return new JDialog((Frame) window);
    } else if (window instanceof Dialog) {
      return new JDialog((Dialog) window);
    } else {
      throw new IllegalArgumentException("Component does not have a window ancestor");
    }
  }

  /**
   * Finds the deepest, visible, active child window from the given window. If an active window can
   * not be found, it will try to search for the deepest, visible child window. If that can't be
   * found, then it will use the given baseWindow.
   *
   * @param baseWindow the parent window.
   * @return the active child window, else the deepest visible window, else the baseWindow.
   */
  public static Window findActiveWindow(Window baseWindow) {
    Window selected = findDeepestChildWindow(baseWindow, true, true);

    if (selected.equals(baseWindow)) {
      selected = findDeepestChildWindow(baseWindow, true, false);
    }

    return selected;
  }

  /**
   * Finds the deepest direct child window of the given window that matches the specified
   * attributes.
   *
   * @param baseWindow the parent window.
   * @param isVisible the child window must be visible or not.
   * @param isActive the child window must be active or not.
   * @return the deepest child window matching the attributes. This may be the given baseWindow.
   */
  public static Window findDeepestChildWindow(
      Window baseWindow, boolean isVisible, boolean isActive) {
    Window last = baseWindow;
    Window next = null;

    while (true) {
      next = findChildWindow(last, true, true);
      if (next == null) {
        return last;
      } else {
        last = next;
      }
    }
  }

  /**
   * Finds the first direct child window of the given window that matches the specified attributes.
   *
   * @param baseWindow the parent window.
   * @param isVisible the child window must be visible or not.
   * @param isActive the child window must be active or not.
   * @return a child window matching the attributes, or null if none found.
   */
  public static Window findChildWindow(Window baseWindow, boolean isVisible, boolean isActive) {
    Window ownedWindows[] = baseWindow.getOwnedWindows();
    for (int i = 0; i < ownedWindows.length; ++i) {
      Window w = ownedWindows[i];

      // If this is an active Frame or Dialog then start again from it:
      if ((w instanceof Frame) || ((w instanceof Dialog) && ((Dialog) w).isModal())) {
        boolean check1 = !(isVisible ^ w.isVisible());
        boolean check2 = !(isActive ^ w.isActive());

        if (check1 && check2) {
          return ownedWindows[i];
        }
      }
    }

    return null;
  }
}
