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

package com.tle.client.gui.popup;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public abstract class AbstractPopupListener extends MouseAdapter {
  private final JPopupMenu menu;

  public AbstractPopupListener(Action... actions) {
    menu = new JPopupMenu();
    for (Action action : actions) {
      menu.add(new JMenuItem(action));
    }
  }

  public AbstractPopupListener(Collection<? extends Action> actions) {
    menu = new JPopupMenu();
    for (Action action : actions) {
      menu.add(new JMenuItem(action));
    }
  }

  public AbstractPopupListener(JPopupMenu menu) {
    this.menu = menu;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    checkPopup(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    checkPopup(e);
  }

  private void checkPopup(MouseEvent e) {
    if (e.isPopupTrigger()) {
      selectItemUnderMouse(e);

      Component source = (Component) e.getSource();
      menu.show(source, e.getX(), e.getY());
    }
  }

  public abstract void selectItemUnderMouse(MouseEvent e);
}
