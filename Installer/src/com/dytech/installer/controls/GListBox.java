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

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;
import com.dytech.installer.Item;
import java.awt.Component;
import java.util.Iterator;
import javax.swing.JComboBox;
import javax.swing.JComponent;

public class GListBox extends GuiControl {
  protected JComboBox combobox;

  public GListBox(PropBagEx controlBag) throws InstallerException {
    super(controlBag);
  }

  @Override
  public JComponent generateControl() {
    combobox = new JComboBox(items);
    combobox.setAlignmentX(Component.LEFT_ALIGNMENT);

    Iterator i = items.iterator();
    while (i.hasNext()) {
      Item item = (Item) i.next();
      if (item.isSelected()) {
        combobox.setSelectedItem(item);
      }
    }

    return combobox;
  }

  @Override
  public void loadControl(PropBagEx xml) {
    if (xml != null || targets.isEmpty()) {
      String target = (String) targets.get(0);
      String value = xml.getNode(target);
      if (value.length() > 0) {
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
          Item item = (Item) iter.next();
          item.setSelected(item.getValue().equals(value));
        }
      }
    }
  }

  @Override
  public String getSelection() {
    Item item = (Item) combobox.getSelectedItem();
    return item.getValue();
  }
}
