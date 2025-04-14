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

import com.tle.common.recipientselector.formatter.ExpressionFormatter;
import com.tle.core.remoting.RemoteUserService;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class ExpressionListCellRenderer extends DefaultListCellRenderer {
  private static final long serialVersionUID = 1L;
  private ExpressionFormatter formatter;

  public ExpressionListCellRenderer(RemoteUserService userService) {
    formatter = new ExpressionFormatter(userService);
  }

  @Override
  public Component getListCellRendererComponent(
      JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    setText(formatter.convertToInfix(getExpression(value)));
    return this;
  }

  public String getExpression(Object value) {
    if (value instanceof String) {
      return (String) value;
    }
    throw new IllegalStateException("We should not have reached here");
  }
}
