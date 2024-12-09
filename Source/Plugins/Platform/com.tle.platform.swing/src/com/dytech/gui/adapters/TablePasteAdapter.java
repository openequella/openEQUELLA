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

package com.dytech.gui.adapters;

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * TablePasteAdapter enables Paste Clipboard functionality on JTables. The clipboard data format
 * used by the adapter is compatible with the clipboard format used by Excel. This provides for
 * clipboard interoperability between enabled JTables and Excel.
 */
public final class TablePasteAdapter implements ActionListener {
  private JTable table;
  private TablePasteModel model;
  private Clipboard clipboard;
  private Map<Class<?>, DataConverter> converters;
  private boolean selectOnPaste;

  public static TablePasteAdapter apply(final JTable table) {
    return new TablePasteAdapter(table);
  }

  public static TablePasteAdapter createTable(TableModel model) {
    return apply(createRawTable(model));
  }

  public static JTable createRawTable(TableModel model) {
    JTable table =
        new JTable(model) {
          @Override
          public boolean getScrollableTracksViewportHeight() {
            // fetch the table's parent
            Container viewport = getParent();

            // if the parent is not a viewport, calling this isn't useful
            if (!(viewport instanceof JViewport)) {
              return false;
            }

            // return true if the table's preferred height is smaller
            // than the viewport height, else false
            return getPreferredSize().height < viewport.getHeight();
          }
        };
    return table;
  }

  public JTable getTable() {
    return table;
  }

  /**
   * The TablePasteAdapter is constructed with a JTable on which it enables Paste and acts as a
   * Clipboard listener.
   */
  private TablePasteAdapter(final JTable table) {
    this.table = table;

    // Try getting the table model
    TableModel theirModel = table.getModel();
    if (theirModel instanceof TablePasteModel) {
      model = (TablePasteModel) theirModel;
    } else if (theirModel instanceof DefaultTableModel) {
      model = new DefaultTableModelAdapter((DefaultTableModel) theirModel);
    } else {
      throw new ClassCastException(
          "Table model must implement TablePasteModel or extend DefaultTableModel"); //$NON-NLS-1$
    }

    // Register [Ctrl] + [C] for copy.
    KeyStroke copy =
        KeyStroke.getKeyStroke(
            KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false);
    table.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED); // $NON-NLS-1$
    // Register [Ctrl] + [V] for paste.
    KeyStroke paste =
        KeyStroke.getKeyStroke(
            KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false);
    table.registerKeyboardAction(this, "Paste", paste, JComponent.WHEN_FOCUSED); // $NON-NLS-1$

    // Get a reference to the clipboard
    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    // Set some defaults..
    setSelectOnPaste(true);

    // Setup converters.
    converters = new HashMap<Class<?>, DataConverter>();
    register(Boolean.class, new BooleanConverter());

    // Ensure that we can still paste into empty tables
    table.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            if (e.isConsumed() || !table.isEnabled() || !SwingUtilities.isLeftMouseButton(e)) {
              return;
            }

            // if the UI ignores our desparate plea for focus when
            // the table is clicked beyond the last row or last column,
            // we'll try to grab focus anyway so that CTRL-V works
            int row = table.rowAtPoint(e.getPoint());
            int column = table.columnAtPoint(e.getPoint());
            if (row == -1 || column == -1) {
              if (!table.hasFocus() && table.isRequestFocusEnabled()) {
                table.requestFocus();
              }
            }
          }
        });
  }

  /**
   * Registers a data converter.
   *
   * @param klass The column class.
   * @param converter The data converter.
   */
  public void register(Class<?> klass, DataConverter converter) {
    converters.put(klass, converter);
  }

  /**
   * Unregisters a data converter.
   *
   * @param klass The column class.
   */
  public void unregister(Class<?> klass) {
    converters.remove(klass);
  }

  /**
   * @return Returns the selectOnPaste.
   */
  public boolean isSelectOnPaste() {
    return selectOnPaste;
  }

  /**
   * @param selectOnPaste The selectOnPaste to set.
   */
  public void setSelectOnPaste(boolean selectOnPaste) {
    this.selectOnPaste = selectOnPaste;
  }

  /*
   * (non-Javadoc)
   * @see
   * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().compareTo("Copy") == 0) // $NON-NLS-1$
    {
      StringBuilder sbf = new StringBuilder();

      int[] rows = table.getSelectedRows();
      int cols = table.getColumnCount();
      for (int i = 0; i < rows.length; i++) {
        for (int j = 0; j < cols; j++) {
          if (j > 0) {
            sbf.append('\t');
          }
          sbf.append(serialise(table.getColumnClass(j), table.getValueAt(rows[i], j)));
        }
        sbf.append('\n');
      }
      StringSelection ss = new StringSelection(sbf.toString());
      clipboard.setContents(ss, ss);
    } else if (e.getActionCommand().compareTo("Paste") == 0) // $NON-NLS-1$
    {
      // Try retrieving the clipboard data.
      String data = null;
      try {
        data = (String) (clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor));
      } catch (Exception ex) {
        return;
      }

      // Parse the rows into a nice format
      List<List<Object>> rows = parseTSV(data);

      // Default to inserting at end of table.
      int startRow = model.getRowCount();

      // Determine if we want to insert the data in between rows.
      int[] selectedRows = table.getSelectedRows();
      if (selectedRows.length > 0) {
        startRow = selectedRows[selectedRows.length - 1];
        startRow++;
      }

      // Insert the rows
      int insertAtRow = startRow;
      for (List<Object> row : rows) {
        model.insertRow(insertAtRow, row);
        insertAtRow++;
      }

      if (selectOnPaste) {
        table.getSelectionModel().setSelectionInterval(startRow, insertAtRow - 1);
      }
    }
  }

  /**
   * Splits the cell values into a collection of vectors.
   *
   * @param data a string of tab-separated-values.
   * @return a collection of vectors containing cell values.
   */
  private List<List<Object>> parseTSV(String data) {
    List<List<Object>> rows = new ArrayList<List<Object>>();

    // For each row...
    String[] rowSplit = data.split("\\n"); // $NON-NLS-1$
    for (int i = 0; i < rowSplit.length; i++) {
      List<Object> cells = new ArrayList<Object>();

      // For each cell...
      int maxColumn = model.getColumnCount();
      String[] cellSplit = rowSplit[i].split("\\t"); // $NON-NLS-1$
      for (int j = 0; j < cellSplit.length && j < maxColumn; j++) {
        cells.add(deserialise(model.getColumnClass(j), cellSplit[j]));
      }

      // Add the cells as a new row
      if (!cells.isEmpty()) {
        rows.add(cells);
      }
    }

    return rows;
  }

  private Object deserialise(Class<?> klass, String value) {
    DataConverter converter = converters.get(klass);
    if (converter != null) {
      return converter.deserialise(value);
    } else {
      return value;
    }
  }

  private String serialise(Class<?> klass, Object value) {
    DataConverter converter = converters.get(klass);
    if (converter != null) {
      return converter.serialise(value);
    } else {
      return value.toString();
    }
  }

  /** An interface that all DataConverters must implement. */
  public interface DataConverter {
    Object deserialise(String value);

    String serialise(Object object);
  }

  /** A default converter for the Boolean class. */
  public static class BooleanConverter implements DataConverter {
    @Override
    public Object deserialise(String value) {
      return Boolean.valueOf(value);
    }

    @Override
    public String serialise(Object object) {
      return object.toString();
    }
  }
}
