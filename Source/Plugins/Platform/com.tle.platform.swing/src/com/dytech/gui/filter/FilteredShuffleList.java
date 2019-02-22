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

package com.dytech.gui.filter;

import com.dytech.gui.TableLayout;
import com.tle.common.gui.models.GenericListModel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

public class FilteredShuffleList<T> extends JComponent implements ActionListener {
  private FilterModel<T> filterModel;
  private String title;

  private JButton search;
  private JButton remove;
  private JList list;
  private GenericListModel<T> model;
  private JScrollPane scroller;
  private Comparator<? super T> sorter;

  public FilteredShuffleList(String title, FilterModel<T> filterModel) {
    this(title, filterModel, null);
  }

  public FilteredShuffleList(
      String title, FilterModel<T> filterModel, Comparator<? super T> sorter) {
    this.title = title;
    this.filterModel = filterModel;
    this.sorter = sorter;

    createGUI();
  }

  public GenericListModel<T> getModel() {
    return model;
  }

  public void setListCellRenderer(ListCellRenderer lcr) {
    list.setCellRenderer(lcr);
  }

  @Override
  public void setEnabled(boolean b) {
    search.setEnabled(b);
    remove.setEnabled(b);
    list.setEnabled(b);
    scroller.setEnabled(b);
  }

  public void addItem(T item) {
    model.add(item);
    ensureSort();
  }

  public void addItems(T[] items) {
    for (T item : items) {
      model.add(item);
    }
    ensureSort();
  }

  public void addItems(Collection<T> items) {
    model.addAll(items);
    ensureSort();
  }

  public boolean removeItem(T item) {
    return model.remove(item);
  }

  public void removeItemAt(int index) {
    model.remove(index);
  }

  public void removeAllItems() {
    model.clear();
  }

  public int getItemCount() {
    return model.getSize();
  }

  public Object getItemAt(int index) {
    return model.get(index);
  }

  public java.util.List<T> getItems() {
    return model;
  }

  private void createGUI() {
    search = new JButton("Search...");
    remove = new JButton("Remove");

    remove.addActionListener(this);
    search.addActionListener(this);

    model = new GenericListModel<T>();
    list = new JList(model);
    scroller = new JScrollPane(list);

    final int width = search.getPreferredSize().width;
    final int height = search.getPreferredSize().height;
    final int[] rows = new int[] {height, height, TableLayout.FILL};
    final int[] cols = new int[] {width, TableLayout.FILL};

    setLayout(new TableLayout(rows, cols, 5, 5));

    add(search, new Rectangle(0, 0, 1, 1));
    add(remove, new Rectangle(0, 1, 1, 1));
    add(scroller, new Rectangle(1, 0, 1, 3));
  }

  public void setSearchText(String text) {
    search.setText(text);
  }

  public void setRemoveText(String text) {
    remove.setText(text);
  }

  /*
   * (non-Javadoc)
   * @see
   * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == search) {
      filterModel.setExclusion(getItems());

      FilterDialog<T> dialog = new FilterDialog<T>(title, filterModel);
      dialog.setListCellRenderer(list.getCellRenderer());
      List<T> results = dialog.showDialog(this);
      if (results != null) {
        addItems(results);
      }
    } else if (e.getSource() == remove) {
      if (!list.isSelectionEmpty()) {
        final int[] indices = list.getSelectedIndices();
        for (int i = indices.length - 1; i >= 0; i--) {
          removeItemAt(indices[i]);
        }
        list.updateUI();
      }
    }
  }

  private void ensureSort() {
    if (sorter != null) {
      Collections.sort(model, sorter);
    }
  }
}
