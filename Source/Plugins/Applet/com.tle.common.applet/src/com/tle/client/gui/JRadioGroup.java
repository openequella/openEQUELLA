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

package com.tle.client.gui;

import com.dytech.gui.VerticalFlowLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class JRadioGroup extends JPanel implements ItemListener {
  private static final long serialVersionUID = 1L;
  private final JRadioButton radio;

  public JRadioGroup(String name) {
    this(name, new VerticalFlowLayout());
  }

  public JRadioGroup(String name, LayoutManager layout) {
    this(name, layout, null);
  }

  public JRadioGroup(String name, LayoutManager layout, Rectangle radioPosition) {
    radio = new JRadioButton(name);
    init(layout, radioPosition);
  }

  public void setSelected(boolean b) {
    radio.setSelected(b);
    setEnabled(b);
  }

  public boolean isSelected() {
    return radio.isSelected();
  }

  public JRadioButton getButton() {
    return radio;
  }

  private void init(LayoutManager layout, Rectangle radioPosition) {
    setLayout(layout);
    if (radioPosition == null) {
      add(radio);
    } else {
      add(radio, radioPosition);
    }
    radio.addItemListener(this);
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    boolean selected = radio.isSelected();
    setEnabled(selected);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    recurse(this, enabled);
    radio.setEnabled(true);
  }

  public void setTotallyEnabled(boolean enabled) {
    setEnabled(enabled);
    radio.setEnabled(enabled);
  }

  protected void recurse(Container container, boolean enabled) {
    for (Component comp : container.getComponents()) {
      comp.setEnabled(enabled);
      if (comp instanceof Container /*
										 * && !(comp instanceof
										 * JChangeDetectorPanel)
										 */ && !(comp instanceof JComboBox)) {
        recurse((Container) comp, enabled);
      }
    }
  }
}
