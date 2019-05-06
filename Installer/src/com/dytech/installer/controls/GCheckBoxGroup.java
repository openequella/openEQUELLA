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

package com.dytech.installer.controls;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;
import com.dytech.installer.Item;
import java.util.Iterator;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;

public class GCheckBoxGroup extends GAbstractButtonGroup {
  public GCheckBoxGroup(PropBagEx controlBag) throws InstallerException {
    super(controlBag);
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.controls.GAbstractButtonGroup#generateButton(
   * java.lang.String, javax.swing.ButtonGroup)
   */
  @Override
  public AbstractButton generateButton(String name, ButtonGroup group) {
    return new JCheckBox(name);
  }

  /*
   * (non-Javadoc)
   * @see
   * com.dytech.installer.controls.GuiControl#saveToTargets(com.dytech.devlib
   * .PropBagEx)
   */
  @Override
  public void saveToTargets(PropBagEx outputBag) {
    Iterator i = targets.iterator();
    while (i.hasNext()) {
      String baseTarget = (String) i.next();
      Iterator j = items.iterator();
      while (j.hasNext()) {
        Item item = (Item) j.next();

        String target = baseTarget + "/" + item.getValue();
        String value = item.getButton().isSelected() ? "true" : "false";

        outputBag.deleteNode(target);
        outputBag.createNode(target, value);
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.controls.GuiControl#getSelection()
   */
  @Override
  public String getSelection() {
    return new String();
  }
}
