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

package com.dytech.gui.flatter;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

public class FlatterRadioButtonUI extends FlatterCheckBoxUI {
  private static final FlatterRadioButtonUI m_buttonUI = new FlatterRadioButtonUI();

  public FlatterRadioButtonUI() {
    // Nothing to do here
  }

  public static ComponentUI createUI(JComponent c) {
    return m_buttonUI;
  }

  @Override
  public synchronized void installUI(JComponent c) {
    super.installUI(c);

    mBackgroundNormal = UIManager.getColor("RadioButton.background");
    mBackgroundPressed = UIManager.getColor("RadioButton.backgroundPressed");
    mBackgroundActive = UIManager.getColor("RadioButton.backgroundActive");
    mTextNormal = UIManager.getColor("RadioButton.textNormal");
    mTextPressed = UIManager.getColor("RadioButton.textPressed");
    mTextActive = UIManager.getColor("RadioButton.textActive");
    mTextDisabled = UIManager.getColor("RadioButton.textDisabled");
    mIconChecked = UIManager.getIcon("RadioButton.iconChecked");
    mIconUnchecked = UIManager.getIcon("RadioButton.iconUnchecked");
    mIconPressedChecked = UIManager.getIcon("RadioButton.iconPressedChecked");
    mIconPressedUnchecked = UIManager.getIcon("RadioButton.iconPressedUnchecked");

    c.setBackground(mBackgroundNormal);
    c.addMouseListener(this);
  }
}
