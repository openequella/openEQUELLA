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

package com.dytech.installer.controls;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;
import com.dytech.installer.Item;

public class GEditBox extends GuiControl {
  protected JTextField field;

  public GEditBox(PropBagEx controlBag) throws InstallerException {
    super(controlBag);
  }

  @Override
  public String getSelection() {
    return field.getText();
  }

  @Override
  public JComponent generateControl() {
    field = new JTextField();
    field.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));

    if (items.size() >= 1) {
      field.setText(((Item) items.get(0)).getValue());
    }

    return field;
  }
}
