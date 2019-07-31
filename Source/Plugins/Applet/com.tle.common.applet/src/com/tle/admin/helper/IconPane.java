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

package com.tle.admin.helper;

import com.dytech.gui.JImage;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Created Jan 14, 2004
 *
 * @author Nicholas Read
 */
public class IconPane extends JPanel implements MouseListener {
  private static final long serialVersionUID = 1L;
  private ListSelectionListener listener;
  private IconContainer icons;
  private JImage selection;

  public IconPane(IconContainer icons) {
    this.icons = icons;

    setup();
  }

  private void setup() {
    setLayout(new FlowLayout(FlowLayout.LEFT));
    setBackground(Color.WHITE);
    setMaximumSize(new Dimension(0, 1000));
    setPreferredSize(new Dimension(0, 1000));
    setMinimumSize(new Dimension(0, 1000));

    Iterator<JImage> i = icons.iterateImages();
    while (i.hasNext()) {
      JImage image = i.next();
      add(image);
      image.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
      image.addMouseListener(this);
    }
  }

  public void setSelectionListener(ListSelectionListener l) {
    listener = l;
  }

  public JImage getSelectedImage() {
    return selection;
  }

  public String getSelectedPath() {
    return icons.getPath(selection);
  }

  public void unregisterIcons() {
    if (selection != null) {
      selection.setBorder(null);
    }

    Iterator<JImage> i = icons.iterateImages();
    while (i.hasNext()) {
      JImage image = i.next();
      image.removeMouseListener(this);
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (selection != null) {
      selection.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
    }

    selection = (JImage) e.getSource();
    selection.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));

    if (listener != null) {
      listener.valueChanged(new ListSelectionEvent(this, 0, 0, false));
    }
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    // We don't care about this event
  }

  @Override
  public void mouseExited(MouseEvent e) {
    // We don't care about this event
  }

  @Override
  public void mousePressed(MouseEvent e) {
    // We don't care about this event
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    // We don't care about this event
  }
}
