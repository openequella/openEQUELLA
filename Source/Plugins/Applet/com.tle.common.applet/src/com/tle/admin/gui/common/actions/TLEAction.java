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

package com.tle.admin.gui.common.actions;

import com.tle.common.Check;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

public abstract class TLEAction extends AbstractAction {
  public TLEAction() {
    super();
  }

  public TLEAction(String displayName) {
    putValue(Action.NAME, displayName);
  }

  public TLEAction(String displayName, String icon) {
    this(displayName);
    setIcon(icon);
  }

  public void setShortDescription(String description) {
    putValue(Action.SHORT_DESCRIPTION, description);
  }

  public void setIcon(URL path) {
    Check.checkNotNull(path);

    putValue(Action.SMALL_ICON, new ImageIcon(path));
  }

  public void setIcon(Class<?> base, String path) {
    setIcon(base.getResource(path));
  }

  public void setIcon(String path) {
    setIcon(this.getClass(), path);
  }

  public void setMnemonic(int keyEvent) {
    putValue(MNEMONIC_KEY, keyEvent);
  }

  public void update() {
    // To be overridden
  }

  public KeyStroke invokeForWindowKeyStroke() {
    return null;
  }
}
