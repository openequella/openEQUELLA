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

package com.tle.client.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;

public class JLinkButton extends JLabel implements MouseListener {
  private static final long serialVersionUID = 1L;
  private static final Cursor CLICKABLE;
  private static final Cursor NOT_CLICKABLE;

  static {
    CLICKABLE = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    Cursor c;
    try {
      c = Cursor.getSystemCustomCursor("Invalid.32x32"); // $NON-NLS-1$
    } catch (Exception ex) {
      c = Cursor.getDefaultCursor();
    }
    NOT_CLICKABLE = c;
  }

  public JLinkButton() {
    super();
    setup();
  }

  public JLinkButton(String text) {
    super(text);
    setup();
  }

  public void addActionListener(ActionListener l) {
    listenerList.add(ActionListener.class, l);
  }

  public void removeActionListener(ActionListener l) {
    listenerList.remove(ActionListener.class, l);
  }

  private void setup() {
    setForeground(Color.BLUE);
    setCursor(CLICKABLE);
    addMouseListener(this);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);

    setCursor(enabled ? CLICKABLE : NOT_CLICKABLE);
  }

  /*
   * (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseClicked(MouseEvent e) {
    if (e.getSource() == this && isEnabled()) {
      ActionEvent event = new ActionEvent(this, 0, null);
      for (ActionListener listener : listenerList.getListeners(ActionListener.class)) {
        listener.actionPerformed(event);
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseEntered(MouseEvent e) {
    // We don't care about this event
  }

  /*
   * (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseExited(MouseEvent e) {
    // We don't care about this event
  }

  /*
   * (non-Javadoc)
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(MouseEvent e) {
    // We don't care about this event
  }

  /*
   * (non-Javadoc)
   * @see
   * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(MouseEvent e) {
    // We don't care about this event
  }
}
