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

package com.dytech.gui.workers;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * This is the glass pane class that intercepts screen interactions during system busy states. <hr/>
 * This was merged in to Dytech's code library by Nicholas Read. It should be used in conjunction
 * with <code>GlassSwingWorker</code>. Code has been added to stop the window closing down during
 * it's busy state if the window is an instance of <code>JFrame</code> or <code>JDialog</code>.
 *
 * @author Yexin Chen
 * @see com.dytech.gui.workers.GlassSwingWorker
 */
public class BusyGlassPane extends JComponent implements AWTEventListener {
  private Window theWindow;
  private Component activeComponent;
  private final boolean stopClosing;
  private int savedCloseOperation = -1;

  /**
   * GlassPane constructor comment.
   *
   * @param Container a
   */
  protected BusyGlassPane(Component activeComponent, boolean stopClosing) {
    this.stopClosing = stopClosing;

    // add adapters that do nothing for keyboard and mouse actions
    addMouseListener(
        new MouseAdapter() {
          // We don't care about any events
        });

    addKeyListener(
        new KeyAdapter() {
          // We don't care about any events
        });

    setActiveComponent(activeComponent);
  }

  /**
   * Receives all key events in the AWT and processes the ones that originated from the current
   * window with the glass pane.
   *
   * @param event the AWTEvent that was fired
   */
  @Override
  public void eventDispatched(AWTEvent event) {
    Object source = event.getSource();

    // discard the event if its source is not from the correct type
    boolean sourceIsComponent = (event.getSource() instanceof Component);

    if ((event instanceof KeyEvent) && sourceIsComponent) {
      // If the event originated from the window w/glass pane, consume the
      // event
      if ((SwingUtilities.windowForComponent((Component) source) == theWindow)) {
        ((KeyEvent) event).consume();
      }
    }
  }

  /**
   * Set the component that ordered-up the glass pane.
   *
   * @param aComponent the UI component that asked for the glass pane
   */
  private void setActiveComponent(Component aComponent) {
    activeComponent = aComponent;
  }

  /** Sets the glass pane as visible or invisible. The mouse cursor will be set accordingly. */
  @Override
  public void setVisible(boolean value) {
    if (value) {
      // keep track of the visible window associated w/the component
      // useful during event filtering
      if (theWindow == null) {
        if (activeComponent != null) {
          theWindow = SwingUtilities.windowForComponent(activeComponent);
        }
        if (theWindow == null) {
          if (activeComponent instanceof Window) {
            theWindow = (Window) activeComponent;
          }
        }
      }

      if (stopClosing) {
        if (theWindow instanceof JFrame) {
          JFrame frame = (JFrame) theWindow;
          savedCloseOperation = frame.getDefaultCloseOperation();
          frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        } else if (theWindow instanceof JDialog) {
          JDialog dialog = (JDialog) theWindow;
          savedCloseOperation = dialog.getDefaultCloseOperation();
          dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
      }

      // Sets the mouse cursor to hourglass mode
      Container top = getTopLevelAncestor();
      if (top != null) {
        top.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      }

      if (theWindow != null) {
        activeComponent = theWindow.getFocusOwner();
      }

      // Start receiving all events and consume them if necessary
      Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);

      this.requestFocus();

      // Activate the glass pane capabilities
      super.setVisible(value);
    } else {
      if (theWindow != null && stopClosing) {
        if (theWindow instanceof JFrame) {
          JFrame frame = (JFrame) theWindow;
          frame.setDefaultCloseOperation(savedCloseOperation);
        } else if (theWindow instanceof JDialog) {
          JDialog dialog = (JDialog) theWindow;
          dialog.setDefaultCloseOperation(savedCloseOperation);
        }
      }

      // Stop receiving all events
      Toolkit.getDefaultToolkit().removeAWTEventListener(this);

      // Deactivate the glass pane capabilities
      super.setVisible(value);

      // Sets the mouse cursor back to the regular pointer
      if (getTopLevelAncestor() != null) {
        getTopLevelAncestor().setCursor(null);
      }
    }
  }
}
