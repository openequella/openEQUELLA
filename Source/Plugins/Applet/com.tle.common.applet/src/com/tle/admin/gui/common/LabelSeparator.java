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

package com.tle.admin.gui.common;

import com.dytech.gui.TableLayout;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSeparator;

public class LabelSeparator extends JComponent {
  private static final long serialVersionUID = 1L;

  public LabelSeparator(String text) {
    this(new JLabel(text));
  }

  public LabelSeparator(JLabel label) {
    JSeparator separator = new JSeparator();

    final int height1 = label.getPreferredSize().height;
    final int height2 = separator.getPreferredSize().height;
    final int height3 = (height1 - height2) / 2;
    final int width1 = label.getPreferredSize().width;

    final int[] rows = {
      height3, height2, height3,
    };
    final int[] cols = {
      width1, TableLayout.FILL,
    };

    setLayout(new TableLayout(rows, cols));

    add(label, new Rectangle(0, 0, 1, 3));
    add(separator, new Rectangle(1, 1, 1, 1));
  }
}
