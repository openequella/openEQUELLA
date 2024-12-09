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

import java.awt.event.MouseEvent;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

public class TreePopupListener extends AbstractPopupListener {
  private final JTree tree;

  public TreePopupListener(JTree tree, Action... actions) {
    super(actions);
    this.tree = tree;
  }

  public TreePopupListener(JTree tree, Collection<? extends Action> actions) {
    super(actions);
    this.tree = tree;
  }

  public TreePopupListener(JTree tree, JPopupMenu menu) {
    super(menu);
    this.tree = tree;
  }

  @Override
  public void selectItemUnderMouse(MouseEvent e) {
    tree.setSelectionPath(tree.getPathForLocation(e.getX(), e.getY()));
  }
}
