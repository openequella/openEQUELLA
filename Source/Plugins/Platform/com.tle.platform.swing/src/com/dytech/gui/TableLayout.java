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

package com.dytech.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * If you've ever used Java's layout managers, you will know that they are really good at what they
 * do. Unfortunately, they do a real good job of sucking badly and irritating the programmer. This
 * layout manager has been created to provide the simplicity of GridLayout, but with the flexibility
 * of the well-feared GridBagLayout. This basically provides the same functionality of CellLayout
 * (provided in this same directory), but allows for rows and columns to be of different sizes. Best
 * of all, you can specify the sizes yourself, either by fixed pixels, relative sizes, or a mix of
 * the two.
 *
 * <h1>Usage</h1>
 *
 * The following is an example of a simple 3x4 fixed size grid:
 *
 * <pre>
 * int rows[] = new int[]{10, 20, 25, 15};
 * int columns[] = new int[]{100, 100, 550};
 * TableLayout layout = new TableLayout(rows, columns);
 * </pre>
 *
 * You can specify a relative row or column size by using -1. The following example would have 3
 * equally spaced columns across the entire container:
 *
 * <pre>
 * int rows[] = new int[]{10, 20, 25, 15};
 * int columns[] = new int[]{-1, -1, -1};
 * TableLayout layout = new TableLayout(rows, columns);
 * </pre>
 *
 * You can specify the magnitude of the relative size by increasing the magnitude of the negative
 * number (ie, decreasing the number). For example, the following will create three columns, with
 * the second being twice as large as the first, and the third being four times larger than the
 * first (and twice as large as the second).
 *
 * <pre>
 * int rows[] = new int[]{10, 20, 25, 15};
 * int columns[] = new int[]{-1, -2, -4};
 * TableLayout layout = new TableLayout(rows, columns);
 * </pre>
 *
 * You can also use FILL, DOUBLE_FILL, TRIPLE_FILL, and QUADRUPLE_FILL to replace those nasty
 * negative numbers. You can even do mathematics on them.
 *
 * <pre>
 * int MY_DECA_FILL = TableLayout.TRIPLE_FILL * 3 + TableLayout.FILL;
 * int rows[] = new int[]{10, 20, 25, TableLayout.FILL};
 * int columns[] = new int[]{TableLayout.FILL, TableLayout.DOUBLE_FILL, MY_DECA_FILL};
 * TableLayout layout = new TableLayout(rows, columns);
 * </pre>
 *
 * To add items to a layout, you use a rectangle to set the x and y position on the grid, as well as
 * the width (# of columns to span) and height (# of rows to span). You need to use the two
 * parameter method of <code>add</code>.
 *
 * <pre>
 * container.add(someComponent, new Rectangle(0, 1, 3, 2));
 * </pre>
 *
 * <h1>New Features</h1>
 *
 * A host of new features have been add to version 1.1 of this layout manager, but still maintaining
 * backwards compatibility. You can now use PREFERRED for row sizes. This will use the largest
 * preferred size of all components on the row, except for components that also span one or more
 * variable fill rows. You can also use INVISIBLE for rows or columns, which is equivalent to
 * creating the row or column with a size of zero. Row and column sizes can now be changed
 * dynamically, making this layout perfect for providing hiding/showing tabs or sections.
 *
 * @author Nicholas Read
 * @version 1.1
 */
public class TableLayout implements LayoutManager2 {
  public static final int FILL = -1;
  public static final int DOUBLE_FILL = FILL * 2;
  public static final int TRIPLE_FILL = FILL * 3;
  public static final int QUADRUPLE_FILL = FILL * 4;
  public static final int INVISIBLE = 0;
  public static final int PREFERRED = Integer.MAX_VALUE;

  private static final String ERROR_CONSTRAINT_NOT_RECTANGLE =
      "Constraint must be an instance of java.awt.Rectangle"; //$NON-NLS-1$
  private static final String ERROR_CONSTRAINT_OUT_OF_BOUNDS =
      "Constraint is placing the component outside of the layouts defined rows and"
          + " columns"; //$NON-NLS-1$
  private static final String ERROR_ADD_BY_STRING_NOT_SUPPORTED =
      "You need to add the component with a Rectangle as a constraint, not a String!"; //$NON-NLS-1$
  private static final int DEFAULT_CELL_GAP = 5;

  private List<Rectangle> bounds = new ArrayList<Rectangle>();
  private List<Component> components = new ArrayList<Component>();

  protected int[] columns;
  private int columnGap;
  private int columnFixedTotal;
  private int columnVariableParts;

  protected int[] rows;
  private int rowGap;
  private int rowFixedTotal;
  private int rowVariableParts;

  private boolean recalculationRequired;

  /**
   * Constructs a <code>TableLayout</code> with the given row and column sizes. Please consult this
   * classes JavaDoc for information on specifying row and column sizes.
   */
  public TableLayout(int[] rows, int[] columns) {
    this(rows, columns, DEFAULT_CELL_GAP, DEFAULT_CELL_GAP);
  }

  /**
   * Constructs a <code>TableLayout</code> with the given row and column sizes. Please consult this
   * classes JavaDoc for information on specifying row and column sizes.
   */
  public TableLayout(int[] rows, int[] columns, int rowGap, int columnGap) {
    setRows(rows);
    setColumns(columns);
    setRowGap(rowGap);
    setColumnGap(columnGap);
  }

  /*
   * (non-Javadoc)
   * @see java.awt.LayoutManager2#addLayoutComponent(java.awt.Component,
   * java.lang.Object)
   */
  @Override
  public void addLayoutComponent(Component comp, Object constraint) {
    checkConstraint(constraint);

    components.add(comp);
    bounds.add((Rectangle) constraint);
  }

  /**
   * Checks if the given constraint will fit in the given rows and columns.
   *
   * @param constraint The constraint to check.
   * @throws IllegalArgumentException if the constraint is bad.
   */
  private void checkConstraint(Object constraint) {
    if (!(constraint instanceof Rectangle)) {
      throw new IllegalArgumentException(ERROR_CONSTRAINT_NOT_RECTANGLE);
    }

    Rectangle locBounds = (Rectangle) constraint;
    int maxColumn = locBounds.x + locBounds.width;
    int maxRow = locBounds.y + locBounds.height;

    // Check if the constraint is inside the maximum bounds.
    if (maxColumn <= columns.length) {
      if (maxRow <= rows.length) {
        return;
      }
    }

    // If we get to here, the constraint is outside the bounds of the
    // layout.
    throw new IllegalArgumentException(ERROR_CONSTRAINT_OUT_OF_BOUNDS);
  }

  /**
   * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
   * @deprecated Do not use this method! It will throw a <code>IllegalAccessError</code>.
   */
  @Override
  @Deprecated
  public void addLayoutComponent(String name, Component comp) {
    throw new IllegalAccessError(ERROR_ADD_BY_STRING_NOT_SUPPORTED);
  }

  /**
   * @return Returns the columnGap.
   */
  public int getColumnGap() {
    return columnGap;
  }

  /**
   * @return Returns the columns.
   */
  public int[] getColumns() {
    return columns;
  }

  /**
   * @return Returns the column size for the given index.
   */
  public int getColumnSize(int index) {
    return columns[index];
  }

  /*
   * (non-Javadoc)
   * @see java.awt.LayoutManager2#getLayoutAlignmentX(java.awt.Container)
   */
  @Override
  public float getLayoutAlignmentX(Container c) {
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see java.awt.LayoutManager2#getLayoutAlignmentY(java.awt.Container)
   */
  @Override
  public float getLayoutAlignmentY(Container c) {
    return 0;
  }

  /**
   * @return Returns the rowGap.
   */
  public int getRowGap() {
    return rowGap;
  }

  /**
   * @return Returns the rows.
   */
  public int[] getRows() {
    return rows;
  }

  /**
   * @return Returns the row size for the given index.
   */
  public int getRowSize(int index) {
    return rows[index];
  }

  /*
   * (non-Javadoc)
   * @see java.awt.LayoutManager2#invalidateLayout(java.awt.Container)
   */
  @Override
  public void invalidateLayout(Container c) {
    forceRecalculation();
  }

  /**
   * Finds the largest height of all the preferred sizes of components that span the given row.
   * Components that span a variable row are not counted.
   *
   * @param row the index of the row to determine the size for.
   * @return the preferred height of the row.
   */
  private int calculatePreferredHeightForRow(int row) {
    int height = 0;

    Iterator<Rectangle> i = bounds.iterator();
    Iterator<Component> j = components.iterator();
    while (i.hasNext() && j.hasNext()) {
      Rectangle position = i.next();
      Component component = j.next();

      if (component.isVisible() && position.contains(position.x, row)) {
        if (!isSpanningVariableRow(position)) {
          int fixed = getTotalFixedHeight(position);
          int leftOver = component.getPreferredSize().height - fixed;

          height = Math.max(height, leftOver);
        }
      }
    }

    return height;
  }

  /**
   * Returns the accumulated height of all the fixed rows that the given bounds span.
   *
   * @param bounds the bounds to check against.
   * @return the height of all fixed rows for the bounds.
   */
  private int getTotalFixedHeight(Rectangle bounds) {
    int total = 0;
    int maxRow = bounds.y + bounds.height;
    for (int r = bounds.y; r < maxRow; r++) {
      if (rows[r] >= 0 && rows[r] != PREFERRED) {
        total += rows[r];
      }
    }
    return total;
  }

  /**
   * Determines if the given bounds is spanning one or more variable rows.
   *
   * @param bounds the bounds to check against.
   * @return true if the bounds span a variable row.
   */
  private boolean isSpanningVariableRow(Rectangle bounds) {
    int maxRow = bounds.y + bounds.height;
    for (int r = bounds.y; r < maxRow; r++) {
      if (rows[r] < 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Performs all the calculations to determine total size of fixed sections, the final size of
   * preferred sections, and the number of variable sections.
   */
  private void calculateTotals() {
    if (recalculationRequired) {
      recalculationRequired = false;

      rowFixedTotal = (rows.length - 1) * rowGap;
      rowVariableParts = 0;

      for (int i = 0; i < rows.length; i++) {
        if (rows[i] == PREFERRED) {
          rowFixedTotal += calculatePreferredHeightForRow(i);

          // We always need to recalculate
          forceRecalculation();
        } else if (rows[i] >= 0) {
          rowFixedTotal += rows[i];
        } else {
          rowVariableParts += -rows[i];
        }
      }

      columnFixedTotal = (columns.length - 1) * columnGap;
      columnVariableParts = 0;

      for (int i = 0; i < columns.length; i++) {
        if (columns[i] >= 0) {
          columnFixedTotal += columns[i];
        } else {
          columnVariableParts += -columns[i];
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
   */
  @Override
  public void layoutContainer(Container parent) {
    calculateTotals();

    int rowPos[] = new int[rows.length + 1];
    int columnPos[] = new int[columns.length + 1];

    Insets insets = parent.getInsets();

    int totalHeight = parent.getHeight() - insets.top - insets.bottom;
    int totalWidth = parent.getWidth() - insets.left - insets.right;

    // Calculate the size of each variable part
    int rowPart = 0;
    if (rowVariableParts > 0) {
      rowPart = (totalHeight - rowFixedTotal) / rowVariableParts;
    }

    int columnPart = 0;
    if (columnVariableParts > 0) {
      columnPart = (totalWidth - columnFixedTotal) / columnVariableParts;
    }

    // Determine each position of the rows. Sonar's suggestion of arrayCopy
    // is nonsense here.
    rowPos[0] = insets.top;
    for (int i = 1; i < rowPos.length; i++) // NOSONAR
    {
      rowPos[i] = rowPos[i - 1];
      if (rows[i - 1] == PREFERRED) {
        rowPos[i] += calculatePreferredHeightForRow(i - 1);
      } else if (rows[i - 1] >= 0) {
        rowPos[i] += rows[i - 1];
      } else {
        rowPos[i] += (-rows[i - 1] * rowPart);
      }
      rowPos[i] += rowGap;
    }

    // Determine each position of the columns
    columnPos[0] = insets.left;
    for (int i = 1; i < columnPos.length; i++) // NOSONAR
    {
      columnPos[i] = columnPos[i - 1];
      if (columns[i - 1] >= 0) {
        columnPos[i] += columns[i - 1];
      } else {
        columnPos[i] += (-columns[i - 1] * columnPart);
      }
      columnPos[i] += columnGap;
    }

    // Layout each of the components
    Iterator<Rectangle> i = bounds.iterator();
    Iterator<Component> j = components.iterator();
    while (i.hasNext() && j.hasNext()) {
      Rectangle position = i.next();
      Component component = j.next();

      if (component.isVisible()) {
        int x = columnPos[position.x];
        int y = rowPos[position.y];
        int w = columnPos[position.x + position.width] - columnPos[position.x] - columnGap;
        int h = rowPos[position.y + position.height] - rowPos[position.y] - rowGap;
        component.setBounds(new Rectangle(x, y, w, h));
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
   */
  @Override
  public Dimension maximumLayoutSize(Container parent) {
    calculateTotals();
    int width = columnVariableParts > 0 ? Integer.MAX_VALUE : getMinimumWidth(parent);
    int height = rowVariableParts > 0 ? Integer.MAX_VALUE : getMinimumHeight(parent);

    return new Dimension(width, height);
  }

  /*
   * (non-Javadoc)
   * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
   */
  @Override
  public Dimension minimumLayoutSize(Container parent) {
    calculateTotals();
    return new Dimension(getMinimumWidth(parent), getMinimumHeight(parent));
  }

  /*
   * (non-Javadoc)
   * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
   */
  @Override
  public Dimension preferredLayoutSize(Container parent) {
    return minimumLayoutSize(parent);
  }

  /**
   * Returns the minimum height of the layout.
   *
   * @param parent The container that layout is set to.
   * @return the minimum height.
   */
  private int getMinimumHeight(Container parent) {
    Insets insets = parent.getInsets();
    return rowFixedTotal + insets.top + insets.bottom;
  }

  /**
   * Returns the minimum width of the layout.
   *
   * @param parent The container that layout is set to.
   * @return the minimum width.
   */
  private int getMinimumWidth(Container parent) {
    Insets insets = parent.getInsets();
    return columnFixedTotal + insets.left + insets.right;
  }

  /*
   * (non-Javadoc)
   * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
   */
  @Override
  public void removeLayoutComponent(Component comp) {
    int index = components.indexOf(comp);
    if (index >= 0) {
      components.remove(index);
      bounds.remove(index);

      forceRecalculation();
    }
  }

  /**
   * @param columnGap The columnGap to set.
   */
  public void setColumnGap(int columnGap) {
    this.columnGap = columnGap;
    forceRecalculation();
  }

  /**
   * @param columns The columns to set.
   */
  public void setColumns(int[] columns) {
    this.columns = columns;
    forceRecalculation();
  }

  /**
   * @param index The column number to set size of.
   * @param size The size to set the indexed column to.
   */
  public void setColumnSize(int index, int size) {
    columns[index] = size;
    forceRecalculation();
  }

  /**
   * @param rowGap The rowGap to set.
   */
  public void setRowGap(int rowGap) {
    this.rowGap = rowGap;
    forceRecalculation();
  }

  /**
   * @param rows The rows to set.
   */
  public void setRows(int[] rows) {
    this.rows = rows;
    forceRecalculation();
  }

  /**
   * @param index The row number to set size of.
   * @param size The size to set the indexed row to.
   */
  public void setRowSize(int index, int size) {
    rows[index] = size;
    forceRecalculation();
  }

  /**
   * @param recalculationRequired The recalculationRequired to set.
   */
  private void forceRecalculation() {
    recalculationRequired = true;
  }
}
