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

package com.tle.common.accesscontrolbuilder;

import com.tle.common.i18n.CurrentLocale;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ActionTableCellRenderer extends DefaultTableCellRenderer {
  private static final long serialVersionUID = 1L;
  private static final String GRANT = CurrentLocale.get("security.editor.grant"); // $NON-NLS-1$
  private static final String REVOKE = CurrentLocale.get("security.editor.revoke"); // $NON-NLS-1$

  public ActionTableCellRenderer() {
    super();
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    JLabel label =
        (JLabel)
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    if (((Boolean) value).booleanValue()) {
      label.setText(GRANT);
    } else {
      label.setText(REVOKE);
    }
    return label;
  }
}
