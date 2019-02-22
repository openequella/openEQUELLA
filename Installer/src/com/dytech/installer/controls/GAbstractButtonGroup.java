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
import java.util.Iterator;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;

public abstract class GAbstractButtonGroup extends GuiControl {
  public GAbstractButtonGroup(PropBagEx controlBag) throws InstallerException {
    super(controlBag);
  }

  @Override
  public void loadControl(PropBagEx xml) {
    if (xml != null) {
      Iterator i = targets.iterator();
      while (i.hasNext()) {
        String target = (String) i.next();
        String value = xml.getNode(target);

        if (items.size() > 0 && value.length() > 0) {
          for (Iterator j = items.iterator(); j.hasNext(); ) {
            Item item = (Item) j.next();
            boolean selected = item.getValue().equals(value);
            item.setSelected(selected);
          }
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.controls.GuiControl#generateControl()
   */
  @Override
  public JComponent generateControl() {
    ButtonGroup group = new ButtonGroup();

    JPanel buttons = new JPanel();
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));

    Iterator i = items.iterator();
    while (i.hasNext()) {
      Item item = (Item) i.next();

      AbstractButton button = generateButton(item.getName(), group);

      item.setButton(button);
      if (item.isSelected()) {
        button.setSelected(true);
      }

      buttons.add(button);
    }

    return buttons;
  }

  protected abstract AbstractButton generateButton(String name, ButtonGroup group);
}
