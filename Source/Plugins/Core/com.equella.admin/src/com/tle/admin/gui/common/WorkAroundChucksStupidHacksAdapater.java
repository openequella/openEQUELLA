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

package com.tle.admin.gui.common;

import com.dytech.gui.Changeable;
import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class WorkAroundChucksStupidHacksAdapater extends JPanel implements Changeable {
  private static final long serialVersionUID = 1L;
  private final Changeable changeable;

  public WorkAroundChucksStupidHacksAdapater(JComponent component, Changeable changeable) {
    this.changeable = changeable;

    setLayout(new GridLayout(1, 1));
    add(component);
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.gui.Changeable#clearChanges()
   */
  @Override
  public void clearChanges() {
    changeable.clearChanges();
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.gui.Changeable#hasDetectedChanges()
   */
  @Override
  public boolean hasDetectedChanges() {
    return changeable.hasDetectedChanges();
  }
}
