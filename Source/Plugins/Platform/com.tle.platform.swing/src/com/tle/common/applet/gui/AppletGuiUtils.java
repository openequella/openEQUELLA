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

package com.tle.common.applet.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public final class AppletGuiUtils {
  public static final Border DEFAULT_BORDER = BorderFactory.createEmptyBorder(5, 5, 5, 5);

  public static <T> void addItemsToJCombo(JComboBox<T> combo, Collection<T> items) {
    for (T item : items) {
      combo.addItem(item);
    }
  }

  private static <T> int findIndexInCombo(JComboBox<T> combo, T obj) {
    final int count = combo.getItemCount();
    for (int i = 0; i < count; i++) {
      Object comboObject = combo.getItemAt(i);
      if (Objects.equals(comboObject, obj)) {
        return i;
      }
    }
    return -1;
  }

  public static <T> boolean isInJCombo(JComboBox<T> combo, T obj) {
    if (obj != null) {
      return findIndexInCombo(combo, obj) > 0;
    }
    return false;
  }

  public static <T> int selectInJCombo(JComboBox<T> combo, T obj) {
    if (obj != null) {
      int i = findIndexInCombo(combo, obj);
      combo.setSelectedIndex(i);
      return i;
    }
    return -1;
  }

  public static <T> int selectInJCombo(JComboBox<T> combo, T obj, int defaultSelectIndex) {
    int i = selectInJCombo(combo, obj);
    if (i >= 0) {
      return i;
    } else {
      int count = combo.getItemCount();
      if (defaultSelectIndex < count) {
        combo.setSelectedIndex(defaultSelectIndex);
      } else if (count > 0) {
        combo.setSelectedIndex(count - 1);
      }
      return combo.getSelectedIndex();
    }
  }

  public static Dimension getGreatestPreferredSize(JComponent... components) {
    Dimension d = new Dimension();
    for (JComponent comp : components) {
      Dimension compSize = comp.getPreferredSize();
      d.width = Math.max(d.width, compSize.width);
      d.height = Math.max(d.height, compSize.height);
    }
    return d;
  }

  public static ButtonGroup group(JRadioButton... buttons) {
    ButtonGroup g = new ButtonGroup();
    for (JRadioButton b : buttons) {
      g.add(b);
    }
    return g;
  }

  public static class BetterGroup<B extends AbstractButton, V> {
    private final Map<B, V> map = new HashMap<B, V>();
    private ButtonGroup bg;

    @SuppressWarnings("unchecked")
    public BetterGroup(boolean createButtonGroup, Object... objs) {
      for (int i = 0; i < objs.length; i++) {
        B object = (B) objs[i];
        map.put(object, (V) objs[++i]);
      }
      if (createButtonGroup) {
        bg = new ButtonGroup();
        for (B b : map.keySet()) {
          bg.add(b);
        }
      }
    }

    public void addButton(B button, V value) {
      map.put(button, value);
      if (bg != null) {
        bg.add(button);
      }
    }

    public B getSelectedButton() {
      Entry<B, V> e = getSelectedEntry();
      return e == null ? null : e.getKey();
    }

    public boolean isSelectionEmpty() {
      return getSelectedEntry() == null;
    }

    public V getSelectedValue() {
      Entry<B, V> e = getSelectedEntry();
      return e == null ? null : e.getValue();
    }

    public void selectButtonByValue(V value) {
      Entry<B, V> e = getByValue(value);
      if (e != null) {
        e.getKey().setSelected(true);
      }
    }

    public void selectFirstEnabledButton() {
      for (B b : map.keySet()) {
        if (b.isEnabled()) {
          b.setSelected(true);
          return;
        }
      }
    }

    private Entry<B, V> getSelectedEntry() {
      for (Entry<B, V> e : map.entrySet()) {
        if (e.getKey().isSelected()) {
          return e;
        }
      }
      return null;
    }

    public Entry<B, V> getByValue(V value) {
      for (Entry<B, V> e : map.entrySet()) {
        if (Objects.equals(e.getValue(), value)) {
          return e;
        }
      }
      return null;
    }

    public void setEnabledByValue(V value, boolean enabled) {
      getByValue(value).getKey().setEnabled(enabled);
    }

    public int size() {
      return map.size();
    }
  }

  public static JSplitPane createSplitPane() {
    JSplitPane split = new JSplitPane();
    removeBordersFromSplitPane(split);
    return split;
  }

  public static void removeBordersFromSplitPane(JSplitPane split) {
    // remove the border from the split pane
    split.setBorder(null);

    // check the UI. If we can't work with the UI any further, then
    // exit here.
    if (!(split.getUI() instanceof BasicSplitPaneUI)) {
      return;
    }

    // grab the divider from the UI and remove the border from it
    final BasicSplitPaneDivider divider = ((BasicSplitPaneUI) split.getUI()).getDivider();
    if (divider != null) {
      // Taken from http://forums.sun.com/thread.jspa?threadID=566152
      divider.setBorder(
          BorderFactory.createCompoundBorder(
              BorderFactory.createEmptyBorder(0, 0, 0, 0),
              new SplitPaneBorder(UIManager.getColor("SplitPane.background")))); // $NON-NLS-1$
    }
  }

  protected static final class SplitPaneBorder implements Border {
    private final Color color;
    private final Insets NO_INSETS = new Insets(0, 0, 0, 0);
    private final Rectangle r = new Rectangle();

    private SplitPaneBorder(Color color) {
      this.color = color;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      g.setColor(color);
      g.fillRect(x, y, width, height);
      if (c instanceof Container) {
        Container cont = (Container) c;
        for (int i = 0, n = cont.getComponentCount(); i < n; i++) {
          Component comp = cont.getComponent(i);
          comp.getBounds(r);
          Graphics tmpg = g.create(r.x, r.y, r.width, r.height);
          comp.paint(tmpg);
          tmpg.dispose();
        }
      }
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return NO_INSETS;
    }

    @Override
    public boolean isBorderOpaque() {
      return true;
    }
  }

  private AppletGuiUtils() {
    throw new Error();
  }
}
