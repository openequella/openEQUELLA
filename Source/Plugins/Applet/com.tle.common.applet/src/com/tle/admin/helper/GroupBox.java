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

package com.tle.admin.helper;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;

public class GroupBox extends JPanel implements ItemListener {
  private static final long serialVersionUID = 1L;

  protected final JPanel inner;
  protected final AbstractButton button;

  protected boolean allowSetEnable = true;

  public static GroupBox withCheckBox(String title, boolean selected) {
    return new GroupBox(new JCheckBox(title, selected));
  }

  public static GroupBox withRadioButton(String title, boolean selected) {
    return new GroupBox(new JRadioButton(title, selected));
  }

  public GroupBox(final AbstractButton b) {
    button = b;
    button.addItemListener(this);
    button.setOpaque(true);
    button.setBackground(this.getBackground());

    final int buttonHeight = button.getPreferredSize().height;

    inner = new JPanel();
    inner.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(buttonHeight / 2, 5, 5, 5)));

    final SpringLayout layout = new SpringLayout();

    super.setLayout(layout);
    super.add(button);
    super.add(inner);

    layout.putConstraint(SpringLayout.WEST, button, 10, SpringLayout.WEST, this);

    layout.putConstraint(SpringLayout.NORTH, inner, buttonHeight / 2, SpringLayout.NORTH, this);
    layout.putConstraint(SpringLayout.WEST, inner, 0, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.SOUTH, this, 0, SpringLayout.SOUTH, inner);
    layout.putConstraint(SpringLayout.EAST, this, 0, SpringLayout.EAST, inner);
  }

  public void setAllowSetEnable(boolean allowSetEnable) {
    this.allowSetEnable = allowSetEnable;
  }

  public void doClick() {
    button.doClick();
  }

  public JPanel getInnerPanel() {
    return inner;
  }

  public ButtonModel getButtonModel() {
    return button.getModel();
  }

  public boolean isSelected() {
    return button.isSelected();
  }

  public void setSelected(boolean b) {
    button.setSelected(b);
  }

  @Override
  public Component add(final Component c) {
    if (allowSetEnable) {
      c.setEnabled(button.isSelected());
    }

    inner.add(c);
    return c;
  }

  @Override
  public void add(final Component c, final Object constraints) {
    if (allowSetEnable) {
      c.setEnabled(button.isSelected());
    }

    inner.add(c, constraints);
  }

  public void addToGroup(final ButtonGroup group) {
    group.add(button);
  }

  @Override
  public void setEnabled(final boolean enabled) {
    super.setEnabled(enabled);
    button.setEnabled(enabled);

    if (allowSetEnable) {
      setAllEnabled(enabled);
    }
  }

  public void addItemListener(final ItemListener l) {
    listenerList.add(ItemListener.class, l);
  }

  public void removeItemListener(final ItemListener l) {
    listenerList.remove(ItemListener.class, l);
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    if (allowSetEnable) {
      setAllEnabled(e.getStateChange() == ItemEvent.SELECTED);
    }

    for (final ItemListener listener : listenerList.getListeners(ItemListener.class)) {
      listener.itemStateChanged(e);
    }
  }

  public void setAllEnabled(final boolean b) {
    final Component[] all = inner.getComponents();
    for (final Component element : all) {
      element.setEnabled(b);
    }
  }
}
