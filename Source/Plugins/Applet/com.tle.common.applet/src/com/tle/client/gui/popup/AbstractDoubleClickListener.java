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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;

public abstract class AbstractDoubleClickListener extends MouseAdapter {
  private final Action action;

  private boolean checkActionEnabled = true;

  public AbstractDoubleClickListener(Action action) {
    this.action = action;
  }

  public void setCheckActionEnabled(boolean checkActionEnabled) {
    this.checkActionEnabled = checkActionEnabled;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
      selectItemUnderMouse(e);

      if (!checkActionEnabled || action.isEnabled()) {
        String actionCommand = (String) action.getValue(Action.ACTION_COMMAND_KEY);
        action.actionPerformed(
            new ActionEvent(
                e.getSource(),
                ActionEvent.ACTION_PERFORMED,
                actionCommand,
                e.getWhen(),
                e.getModifiers()));
      }
    }
  }

  public abstract void selectItemUnderMouse(MouseEvent e);
}
