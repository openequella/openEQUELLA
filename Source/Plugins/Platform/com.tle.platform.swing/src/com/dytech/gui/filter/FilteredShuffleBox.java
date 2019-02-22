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

import com.dytech.gui.JShuffleBox;
import com.dytech.gui.TableLayout;
import com.tle.common.gui.models.GenericListModel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class FilteredShuffleBox<T> extends JComponent implements ActionListener {
  protected int minimumFilter;
  protected JButton search;
  protected JTextField query;
  protected JShuffleBox<T> shuffle;
  protected FilterModel<T> filter;
  protected boolean rightHasPriority;

  public FilteredShuffleBox(FilterModel<T> search) {
    this(search, 1);
  }

  public FilteredShuffleBox(FilterModel<T> search, int minimumFilter) {
    this(search, null, null, minimumFilter);
  }

  public FilteredShuffleBox(FilterModel<T> search, String leftTitle, String rightTitle) {
    this(search, leftTitle, rightTitle, 1);
  }

  public FilteredShuffleBox(
      FilterModel<T> search, String leftTitle, String rightTitle, int minimumFilter) {
    this.minimumFilter = minimumFilter;
    setupGUI(search, leftTitle, rightTitle);
  }

  public GenericListModel<T> getLeftModel() {
    return shuffle.getLeftModel();
  }

  public GenericListModel<T> getRightModel() {
    return shuffle.getRightModel();
  }

  /**
   * Enabled the right-hand column to have priority over any duplicates that are added to the
   * left-hand column. When a search is performed, if a value already exists in the right-hand
   * column, it will not be added to the left. This is the default operation of this control.
   *
   * @param b true if the right-hand column should have priority.
   */
  public void setRightHasPriority(boolean b) {
    rightHasPriority = b;
  }

  @Override
  public void setEnabled(boolean b) {
    search.setEnabled(b);
    query.setEnabled(b);
    shuffle.setEnabled(b);
  }

  public void addToLeft(T o) {
    shuffle.addToLeft(o);
  }

  public void addToLeft(T[] o) {
    shuffle.addToLeft(o);
  }

  public void addToLeft(Collection<T> c) {
    shuffle.addToLeft(c);
  }

  public void addToRight(T o) {
    shuffle.addToRight(o);
  }

  public void addToRight(T[] o) {
    shuffle.addToRight(o);
  }

  public void addToRight(Collection<T> c) {
    shuffle.addToRight(c);
  }

  public void removeAllFromLeft() {
    shuffle.removeAllFromLeft();
  }

  public void removeAllFromRight() {
    shuffle.removeAllFromRight();
  }

  public List<T> getLeft() {
    return shuffle.getLeft();
  }

  public T getLeftAt(int index) {
    return shuffle.getLeftAt(index);
  }

  public int getLeftCount() {
    return shuffle.getLeftCount();
  }

  public List<T> getRight() {
    return shuffle.getRight();
  }

  public T getRightAt(int index) {
    return shuffle.getRightAt(index);
  }

  public int getRightCount() {
    return shuffle.getRightCount();
  }

  protected void setupGUI(FilterModel<T> filterModel, String leftTitle, String rightTitle) {
    filter = filterModel;

    query = new JTextField();
    search = new JButton("Search"); // $NON-NLS-1$
    query.addActionListener(this);
    search.addActionListener(this);

    if (leftTitle == null || rightTitle == null) {
      shuffle = new JShuffleBox<T>();
    } else {
      shuffle = new JShuffleBox<T>(leftTitle, rightTitle);
    }

    shuffle.setAllowDuplicates(true);

    final int height = query.getPreferredSize().height;
    final int width1 = 200;
    final int width2 = search.getPreferredSize().width;

    final int[] rows = new int[] {height, TableLayout.FILL};
    final int[] columns = new int[] {TableLayout.FILL, width1, width2, TableLayout.FILL};

    setLayout(new TableLayout(rows, columns, 5, 5));
    add(query, new Rectangle(1, 0, 1, 1));
    add(search, new Rectangle(2, 0, 1, 1));
    add(shuffle, new Rectangle(0, 1, 4, 1));

    rightHasPriority = true;
  }

  public void setSearchText(String text) {
    search.setText(text);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == search || e.getSource() == query) {
      shuffle.removeAllFromLeft();

      String q = query.getText().trim();
      if (q.length() >= minimumFilter) {
        filter.setExclusion(getRight());
        Collection<T> results = filter.search(q);
        if (rightHasPriority) {
          for (T item : results) {
            if (rightHasPriority && shuffle.getRightIndexOf(item) == -1) {
              shuffle.addToLeft(item);
            }
          }
        } else {
          shuffle.addToLeft(results);
        }
      } else {
        JOptionPane.showMessageDialog(
            this,
            "You must enter at least "
                + minimumFilter //$NON-NLS-1$
                + " character(s) to search on.",
            "Not Enough Characters",
            JOptionPane.WARNING_MESSAGE); // $NON-NLS-1$ //$NON-NLS-2$
      }
    }
  }
}
