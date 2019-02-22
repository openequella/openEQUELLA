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

import com.tle.admin.gui.common.actions.AddAction;
import com.tle.admin.gui.common.actions.DownAction;
import com.tle.admin.gui.common.actions.RemoveAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.gui.common.actions.UpAction;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;

public class GenericTable<T> extends JTable {
  private static final long serialVersionUID = 1L;
  private List<TLEAction> actions;
  GenericTableModel<T> model;

  public GenericTable(final GenericTableModel<T> model) {
    super(model);
    this.model = model;
    actions = new ArrayList<TLEAction>();
  }

  private TLEAction addAction(TLEAction action) {
    actions.add(action);
    return action;
  }

  public void updateButtons() {
    // Can get here via constructor
    if (actions != null) {
      for (TLEAction action : actions) {
        action.update();
      }
    }
  }

  @Override
  public void valueChanged(ListSelectionEvent e) {
    super.valueChanged(e);
    updateButtons();
  }

  public TLEAction getAddAction() {
    return addAction(
        new AddAction() {
          private static final long serialVersionUID = 1L;

          @Override
          public void actionPerformed(ActionEvent e) {
            editingCanceled(null);
            model.add(getSelectedRow());
          }
        });
  }

  public TLEAction getRemoveAction() {
    return addAction(
        new RemoveAction() {
          private static final long serialVersionUID = 1L;

          @Override
          public void actionPerformed(ActionEvent e) {
            editingCanceled(null);
            for (int i : getSelectedRows()) {
              model.remove(i);
            }
          }

          @Override
          public void update() {
            int[] rows = getSelectedRows();
            setEnabled(rows.length > 0);
          }
        });
  }

  public TLEAction getUpAction() {
    return addAction(
        new UpAction() {
          private static final long serialVersionUID = 1L;

          @Override
          public void actionPerformed(ActionEvent e) {
            editingCanceled(null);
            int row = getSelectedRow();
            model.swapRows(row - 1, row);
            getSelectionModel().setSelectionInterval(row - 1, row - 1);
          }

          @Override
          public void update() {
            int[] rows = getSelectedRows();
            setEnabled(rows.length == 1 && rows[0] > 0);
          }
        });
  }

  public TLEAction getDownAction() {
    return addAction(
        new DownAction() {
          private static final long serialVersionUID = 1L;

          @Override
          public void actionPerformed(ActionEvent e) {
            editingCanceled(null);
            int row = getSelectedRow();
            model.swapRows(row, row + 1);
            getSelectionModel().setSelectionInterval(row + 1, row + 1);
          }

          @Override
          public void update() {
            int[] rows = getSelectedRows();
            setEnabled(rows.length == 1 && rows[0] < getRowCount() - 1);
          }
        });
  }

  public abstract static class GenericTableModel<T> extends AbstractTableModel {
    private List<T> values;
    private final String[] columns;

    public GenericTableModel(String[] columns) {
      this.columns = columns;
      values = new ArrayList<T>(); // Avoid nulls
    }

    public void swapRows(int row1, int row2) {
      Collections.swap(getEntries(), row1, row2);
      fireTableRowsUpdated(row1, row2);
    }

    @Override
    public int getColumnCount() {
      return columns.length;
    }

    public List<T> getEntries() {
      return values;
    }

    public void load(List<T> entries) {
      values = entries;
      fireTableRowsInserted(0, values.size());
    }

    public void remove(int i) {
      values.remove(i);
      fireTableRowsDeleted(i, i);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return true;
    }

    public void add(T entity) {
      insert(entity, -1);
    }

    public void add(int selectedRow) {
      insert(create(), selectedRow);
    }

    public void insert(T object, int selectedRow) {
      if (selectedRow < 0) {
        selectedRow = values.size() - 1;
      }
      selectedRow++;
      values.add(selectedRow, object);
      fireTableRowsInserted(selectedRow, selectedRow);
    }

    @Override
    public int getRowCount() {
      return values.size();
    }

    @Override
    public Object getValueAt(int i, int j) {
      T v = values.get(i);
      return get(v, j);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      T v = values.get(rowIndex);
      set(v, columnIndex, aValue);
    }

    public abstract T create();

    public abstract Object get(T t, int column);

    public abstract void set(T t, int column, Object value);

    @Override
    public String getColumnName(int column) {
      return columns[column];
    }
  }

  public void load(List<T> entries) {
    model.load(entries);
    updateButtons();
  }

  public List<T> getEntries() {
    return model.getEntries();
  }
}
