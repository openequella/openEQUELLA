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

import com.dytech.gui.TableLayout;
import com.tle.admin.gui.common.ComboBoxWithView.MyGenericListModel;
import com.tle.common.gui.models.GenericListModel;
import com.tle.common.i18n.CurrentLocale;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;

public abstract class ComboBoxWithView<
        LIST_TYPE, VIEW_TYPE extends ListWithViewInterface<LIST_TYPE>>
    extends AbstractListWithView<
        LIST_TYPE, ListWithViewInterface<LIST_TYPE>, MyGenericListModel<LIST_TYPE>> {
  private JComboBox<LIST_TYPE> combo;

  public ComboBoxWithView() {
    this(null);
  }

  public ComboBoxWithView(String text) {
    JLabel label = null;
    if (text != null && text.trim().length() > 0) {
      label = new JLabel(text);
    }

    combo = new JComboBox<>(model);
    combo.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            onListSelectionChange();
          }
        });

    final int width1 = label == null ? 0 : label.getPreferredSize().width;

    final int[] rows = {
      TableLayout.PREFERRED, TableLayout.FILL,
    };
    final int[] cols = {
      width1, TableLayout.FILL,
    };

    setLayout(new TableLayout(rows, cols));
    if (label != null) {
      add(label, new Rectangle(0, 0, 1, 1));
      add(combo, new Rectangle(1, 0, 1, 1));
    } else {
      add(combo, new Rectangle(0, 0, 2, 1));
    }
    add(editorContainer, new Rectangle(0, 1, 2, 1));
  }

  public void setCellRenderer(ListCellRenderer<LIST_TYPE> renderer) {
    combo.setRenderer(renderer);
  }

  @Override
  protected MyGenericListModel<LIST_TYPE> createModel() {
    return new MyGenericListModel<>(true);
  }

  @Override
  public int getSelectedIndex() {
    return combo.getSelectedIndex();
  }

  @Override
  protected String getNoSelectionText() {
    return CurrentLocale.get("com.tle.admin.gui.common.comboboxwithview.choose"); // $NON-NLS-1$
  }

  public void setSelectedItem(LIST_TYPE value) {
    combo.setSelectedItem(value);
  }

  public static class MyGenericListModel<LIST_TYPE> extends GenericListModel<LIST_TYPE>
      implements ComboBoxModel<LIST_TYPE> {
    private static final long serialVersionUID = 1L;

    private final boolean selectNoneByDefault;

    private int selectedIndex = -1;

    public MyGenericListModel(boolean selectNoneByDefault) {
      this.selectNoneByDefault = selectNoneByDefault;
    }

    @Override
    public void setSelectedItem(Object anObject) {
      selectedIndex = indexOf(anObject);
    }

    // implements javax.swing.ComboBoxModel
    @Override
    public Object getSelectedItem() {
      return selectedIndex >= 0 ? get(selectedIndex) : null;
    }

    @Override
    public boolean addAll(Collection<? extends LIST_TYPE> c) {
      boolean b = super.addAll(c);
      if (!selectNoneByDefault && selectedIndex < 0 && getSize() > 0) {
        selectedIndex = 0;
        fireContentsChanged(this, -1, -1);
      }
      return b;
    }

    @Override
    public void clear() {
      selectedIndex = -1;
      super.clear();
    }

    @Override
    public LIST_TYPE remove(int index) {
      if (selectedIndex == index) {
        selectedIndex = -1;
      } else if (selectedIndex > index) {
        selectedIndex--;
      }

      return super.remove(index);
    }
  }
}
