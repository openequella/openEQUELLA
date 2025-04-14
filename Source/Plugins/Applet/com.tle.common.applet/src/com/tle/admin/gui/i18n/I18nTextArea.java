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

package com.tle.admin.gui.i18n;

import java.awt.Component;
import java.util.Locale;
import java.util.Set;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

public class I18nTextArea extends I18nTextField {
  private static final long serialVersionUID = 1L;
  private JTextArea ta;

  public I18nTextArea(Set<Locale> defaultLocales) {
    super(defaultLocales);
  }

  @Override
  protected JTextComponent getTextComponent() {
    ta = new JTextArea();
    ta.setLineWrap(true);
    ta.setWrapStyleWord(true);
    return ta;
  }

  public void setTextRows(int rows) {
    ta.setRows(rows);
  }

  @Override
  protected void initialiseLayout(
      String layoutConstraints, String cellConstraint, boolean addTextComponent) {
    super.initialiseLayout(layoutConstraints, "grow", addTextComponent); // $NON-NLS-1$
  }

  @Override
  protected Component prepareTextComponent(JTextComponent component) {
    return new JScrollPane(component);
  }
}
