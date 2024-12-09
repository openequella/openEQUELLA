/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * @author Nicholas Read
 * @author Charles O'Farrell
 */
public class JStatusBar extends JComponent {
  /** Used internally to indicate an empty message. */
  private static final int LEVEL_CLEAR = 0;

  /** Default message level. This should be used by tool tips, etc. */
  public static final int LEVEL_GENERAL = 1;

  /**
   * For more urgent messages. This should be used for information that should override tool tips,
   * etc.
   */
  public static final int LEVEL_IMPORTANT = 2;

  /** For more internal use to override all other levels. */
  public static final int LEVEL_GOD_LIKE = 3;

  /** The default timeout if not specified. The message will never be cleared automatically. */
  public static final long TIMEOUT_NEVER = Long.MIN_VALUE;

  /**
   * Clears the message after 5 seconds if it hasn't already been superceeded by another message.
   */
  public static final long TIMEOUT_5_SECONDS = 5000;

  /**
   * Clears the message after 10 seconds if it hasn't already been superceeded by another message.
   */
  public static final long TIMEOUT_10_SECONDS = TIMEOUT_5_SECONDS * 2;

  /** Used when there is no message. */
  private static final String EMPTY_MESSAGE = " ";

  /** The timer used to keep track of the current message clearing task. */
  private Timer clearMessageTimer;

  /** The current level of the message being shown. */
  private int currentLevel;

  /** The Swing component that stores and shows the message. */
  private JLabel message;

  /** The spinner image. */
  private ImageIcon image;

  /** Status text location - left or right */
  private int location;

  /** Spinner location - left or right */
  private int spinnerLocation;

  /** Left panel */
  private JPanel left;

  /** Right panel */
  private JPanel right;

  /** Spinner */
  private JImage spinner;

  /** Inidicates whether the spinner is visible or not. */
  private boolean spinnerVisible;

  /** Constructs a new status bar. Message and Spinner locations default to left */
  public JStatusBar(ImageIcon image) {
    this(image, SwingConstants.LEFT, SwingConstants.LEFT);
  }

  /** Constructs a new status bar */
  public JStatusBar(ImageIcon image, int messageLocation, int spinnerLocation) {
    this.image = image;
    this.location = messageLocation;
    this.spinnerLocation = spinnerLocation;
    setup();
  }

  /**
   * Clears any existing message that are LEVEL_GENERAL or below. Equivalent to <code>
   * clearMessage(LEVEL_GENERAL)</code>
   */
  public void clearMessage() {
    clearMessage(LEVEL_GENERAL);
  }

  /** Clears any existing message if it is at the given level or below. */
  public void clearMessage(int level) {
    if (level != LEVEL_CLEAR) {
      if (currentLevel <= level) {
        cancelAnyClearMessageActions();
        message.setText(EMPTY_MESSAGE);
        currentLevel = LEVEL_CLEAR;
      }
    }
  }

  /**
   * Clears the message after a certain time has passed.
   *
   * @param timeout the number of milliseconds until the message is cleared.
   */
  public void clearMessage(int level, long timeout) {
    cancelAnyClearMessageActions();
    if (timeout != TIMEOUT_NEVER) {
      if (timeout <= 0) {
        clearMessage(level);
      } else {
        clearMessageTimer = new Timer();
        clearMessageTimer.schedule(new ClearMessageTask(level), timeout);
      }
    }
  }

  /** Sets the message. This is equivalent to <code>setMessage(message, LEVEL_GENERAL)</code> */
  public void setMessage(String message) {
    setMessage(message, LEVEL_GENERAL);
  }

  /**
   * Sets the message. This is equivalent to <code>setMessage(message, level, TIMEOUT_NEVER)</code>
   */
  public void setMessage(String message, int level) {
    setMessage(message, level, TIMEOUT_NEVER);
  }

  /**
   * Sets the message. A message will only be displayed if the level is greater than or equals to
   * the current level.
   */
  public void setMessage(String message, int level, long timeout) {
    if (level >= currentLevel) {
      if (message.trim().length() == 0) {
        clearMessage(level);
      } else {
        currentLevel = level;
        this.message.setText(message);
        clearMessage(level, timeout);
      }
    }
  }

  /** Adds the StatusBar to the bottom of the panel, and returns the new panel. */
  public JPanel attachToPanel(JPanel panel) {
    JPanel outer = new JPanel(new BorderLayout());
    outer.add(panel, BorderLayout.CENTER);
    outer.add(this, BorderLayout.SOUTH);

    return outer;
  }

  @Override
  public void setBackground(Color bg) {
    super.setBackground(bg);
    message.setBackground(bg);
  }

  private void cancelAnyClearMessageActions() {
    if (clearMessageTimer != null) {
      clearMessageTimer.cancel();
    }
  }

  private void setup() {
    left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));

    clearMessageTimer = new Timer();

    spinner = new JImage(image);
    spinner.setIcon(null);

    message = new JLabel();
    message.setBorder(BorderFactory.createEmptyBorder(0, 5, 2, 5));
    message.setOpaque(true);
    message.setForeground(Color.DARK_GRAY);
    clearMessage(LEVEL_GOD_LIKE);

    setBorder(new LineBorder(Color.GRAY, 1, 0, 0, 0));
    setOpaque(true);

    final int[] rows = new int[] {image.getIconHeight()};
    final int[] cols = new int[] {TableLayout.FILL, TableLayout.FILL};
    TableLayout layout = new TableLayout(rows, cols);

    setLayout(layout);

    add(left, new Rectangle(0, 0, 1, 1));
    add(right, new Rectangle(1, 0, 1, 1));

    resetSpinner();
    resetMessage();
  }

  private void resetSpinner() {
    removeComponentFromParent(spinner);
    addToBar(spinner, spinnerLocation, true, true);
  }

  private void resetMessage() {
    removeComponentFromParent(message);
    addToBar(message, location, false, false);
  }

  private void removeComponentFromParent(Component comp) {
    Container container = comp.getParent();
    if (container != null) {
      container.remove(comp);
    }
  }

  /**
   * Adds a component to the status bar in the given location.
   *
   * @param comp
   * @param pos - either SwingConstants.LEFT or SwingConstants.RIGHT
   */
  public void addToBar(JComponent comp, int pos) {
    addToBar(comp, pos, false, true);
    resetMessage();
  }

  private void addToBar(JComponent comp, int pos, boolean priority, boolean separator) {
    JPanel panel = getPanel(pos);
    JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
    sep.setPreferredSize(new Dimension(3, image.getIconHeight()));
    comp.setBorder(new EmptyBorder(0, 5, 0, 5));
    boolean sepFirst = false;
    int loc = -1;

    // This could all be done nicer, but who cares if it works?
    if (pos == SwingConstants.RIGHT) {
      if (priority) {
        sepFirst = true;
      } else {
        loc = 0;
      }
    } else {
      if (priority) {
        loc = 0;
        sepFirst = true;
      }
    }

    if (sepFirst && separator) {
      panel.add(sep, loc);
    }
    panel.add(comp, loc);
    if (!sepFirst && separator) {
      panel.add(sep, loc);
    }
  }

  private JPanel getPanel(int pos) {
    JPanel panel = null;
    if (pos == SwingConstants.LEFT) {
      panel = left;
    } else {
      panel = right;
    }
    return panel;
  }

  /**
   * Shows (or hides) the spinner.
   *
   * @param b true to show the spinner, or false to hide it.
   */
  public void setSpinnerVisible(boolean b) {
    if (b) {
      spinner.setIcon(image);
    } else {
      spinner.setIcon(null);
    }
    spinnerVisible = b;
  }

  /** Indicates whether the spinner is currently showing. */
  public boolean isSpinnerVisible() {
    return spinnerVisible;
  }

  /**
   * @author Nicholas Read
   */
  private class ClearMessageTask extends TimerTask {
    private final int level;

    /**
     * Constructs a new ClearMessageTask.
     *
     * @param level the highest level to clear a message at.
     */
    public ClearMessageTask(int level) {
      this.level = level;
    }

    @Override
    public void run() {
      SwingUtilities.invokeLater(
          new Runnable() {
            /*
             * (non-Javadoc)
             * @see java.lang.Runnable#run()
             */
            @Override
            public void run() {
              clearMessage(level);
            }
          });
    }
  }
}
