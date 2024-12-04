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

package com.dytech.gui.filter;

import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.common.gui.models.GenericListModel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class FilterList<T> extends JComponent implements ActionListener, ListSelectionListener {
  private final FilterModel<T> search;

  private EventListenerList listeners;
  private JTextField filter;
  private JButton button;
  private JList list;
  private GenericListModel<T> model;
  private Comparator<? super T> comparator;

  public FilterList(FilterModel<T> search) {
    this.search = search;
    setupGUI();
  }

  public GenericListModel<T> getModel() {
    return model;
  }

  public void setSortingComparator(Comparator<? super T> comparator) {
    this.comparator = comparator;
  }

  public void setSingleSelectionOnly() {
    list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  }

  public void addListSelectionListener(ListSelectionListener l) {
    listeners.add(ListSelectionListener.class, l);
  }

  public void setListCellRenderer(ListCellRenderer renderer) {
    list.setCellRenderer(renderer);
  }

  public boolean isSelectionEmpty() {
    return list.isSelectionEmpty();
  }

  public T getSelectedValue() {
    int selectedIndex = list.getSelectedIndex();
    if (selectedIndex >= 0) {
      return model.get(selectedIndex);
    } else {
      return null;
    }
  }

  public List<T> getSelectedValues() {
    List<T> results = new ArrayList<T>();
    for (int index : list.getSelectedIndices()) {
      results.add(model.get(index));
    }
    return results;
  }

  protected void setupGUI() {
    listeners = new EventListenerList();

    model = new GenericListModel<T>();
    list = new JList(model);
    list.addListSelectionListener(this);

    JScrollPane scroll = new JScrollPane(list);

    setLayout(new BorderLayout(5, 5));
    add(createTop(), BorderLayout.NORTH);
    add(scroll, BorderLayout.CENTER);
  }

  protected JPanel createTop() {
    filter = new JTextField();
    filter.addActionListener(this);

    button = new JButton("Search");
    button.addActionListener(this);

    JPanel all = new JPanel(new BorderLayout(5, 5));
    all.add(filter, BorderLayout.CENTER);
    all.add(button, BorderLayout.EAST);

    return all;
  }

  public void setSearchText(String text) {
    button.setText(text);
  }

  /*
   * (non-Javadoc)
   * @see
   * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
   * .ListSelectionEvent)
   */
  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (e.getSource() == list) {
      ListSelectionListener[] lsl = listeners.getListeners(ListSelectionListener.class);
      if (lsl.length > 0) {
        ListSelectionEvent e2 =
            new ListSelectionEvent(
                this, e.getFirstIndex(), e.getLastIndex(), e.getValueIsAdjusting());

        for (ListSelectionListener l : lsl) {
          l.valueChanged(e2);
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == button || e.getSource() == filter) {
      model.clear();

      final String pattern = filter.getText();
      GlassSwingWorker<Collection<T>> worker =
          new GlassSwingWorker<Collection<T>>() {
            @Override
            public Collection<T> construct() {
              List<T> results = search.search(pattern);
              if (comparator != null) {
                Collections.sort(results, comparator);
              }
              return results;
            }

            @Override
            public void finished() {
              Collection<T> results = get();
              if (results.isEmpty()) {
                JOptionPane.showMessageDialog(getComponent(), "No results found");
              } else {
                model.addAll(results);
              }
            }
          };
      worker.setComponent(this);
      worker.start();
    }
  }
}
