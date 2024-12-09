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

package com.tle.web.sections.standard.model;

import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.web.sections.ajax.AjaxCaptureOptions;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.PageUniqueId;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.AbstractTable;
import com.tle.web.sections.standard.AbstractTable.Sort;
import java.util.Date;
import java.util.List;

@SuppressWarnings("nls")
public class TableState extends HtmlComponentState {
  private TableHeaderRow headerRow;
  private List<TableRow> rows;
  private Sort[] columnSorts;
  private int filterThreshold = -1; // uses a renderer default value
  private boolean filterable = true;
  private boolean wrap = false;
  private boolean presentation = false;
  private boolean noColGroup;

  protected TableState(String defaultRenderer) {
    super(defaultRenderer);
  }

  public TableState() {
    super("table");
  }

  public TableHeaderRow getHeaderRow() {
    return headerRow;
  }

  public TableHeaderRow setColumnHeadings(Object... columnHeadings) {
    headerRow = new TableHeaderRow(this, columnHeadings);
    return headerRow;
  }

  public Object[] getColumnHeadings() {
    if (headerRow == null) {
      return null;
    }
    return headerRow.getCells().toArray();
  }

  public List<TableRow> getRows() {
    if (rows == null) {
      rows = Lists.newArrayList();
    }
    return rows;
  }

  public void setRows(List<TableRow> rows) {
    this.rows = rows;
  }

  public TableRow addRow(Object... cells) {
    return addActualRow(new TableRow(this, cells));
  }

  private TableRow addActualRow(TableRow row) {
    getRows().add(row);
    return row;
  }

  /**
   * Do not use
   *
   * @param headerRow
   */
  public void setHeaderRow(TableHeaderRow headerRow) {
    this.headerRow = headerRow;
  }

  public TableHeaderRow addHeaderRow(Object... cells) {
    headerRow = new TableHeaderRow(this, cells);
    return headerRow;
  }

  public Sort[] getColumnSorts() {
    return columnSorts;
  }

  public void setColumnSorts(Sort... columnSorts) {
    this.columnSorts = columnSorts;
  }

  public int getFilterThreshold() {
    return filterThreshold;
  }

  public void setFilterThreshold(int filterThreshold) {
    this.filterThreshold = filterThreshold;
  }

  public boolean isFilterable() {
    return filterable;
  }

  public void setFilterable(boolean filterable) {
    this.filterable = filterable;
  }

  public abstract static class BaseTableRow<C extends TableCell> extends TagState {
    private final List<C> cells;

    protected BaseTableRow(ElementId parent, C... cells) {
      super(
          new AppendedElementId(
              parent, new AppendedElementId(new SimpleElementId("tr"), new PageUniqueId())));
      registerUse(); // force rendering of row ID
      this.cells = Lists.newArrayList(cells);
    }

    protected BaseTableRow(ElementId parent, Object... cells) {
      super(
          new AppendedElementId(
              parent, new AppendedElementId(new SimpleElementId("tr"), new PageUniqueId())));
      registerUse(); // force rendering of row ID

      this.cells = Lists.newArrayList();
      for (Object cell : cells) {
        this.cells.add(convertCell(cell));
      }
    }

    public List<C> getCells() {
      return cells;
    }

    public C addCell(C cell) {
      if (cell == null) {
        C convertCell = createEmptyCell();
        cells.add(convertCell);
        return convertCell;
      }
      cells.add(cell);
      return cell;
    }

    public C addCell(Object cell) {
      C convertCell = convertCell(cell);
      cells.add(convertCell);
      return convertCell;
    }

    protected abstract C convertCell(Object cell);

    protected abstract C createEmptyCell();
  }

  public static class TableRow extends BaseTableRow<TableCell> {
    private AjaxCaptureOptions ajaxCaptureOptions;

    protected TableRow(ElementId parent, TableCell... cells) {
      super(parent, cells);
    }

    protected TableRow(ElementId parent, Object... cells) {
      super(parent, cells);
    }

    @Override
    protected TableCell convertCell(Object cell) {
      return AbstractTable.convertCell(cell);
    }

    @Override
    protected TableCell createEmptyCell() {
      return AbstractTable.convertCell(null);
    }

    public void setSortData(Object... sortData) {
      List<TableCell> cells = getCells();
      for (int i = 0; i < sortData.length; i++) {
        cells.get(i).setSortData(sortData[i]);
      }
    }

    public void setFilterData(Object filterData) {
      List<TableCell> cells = getCells();
      if (!Check.isEmpty(cells)) {
        cells.get(0).setFilterData(filterData);
      }
    }

    public AjaxCaptureOptions getAjaxCaptureOptions() {
      return ajaxCaptureOptions;
    }

    public void setAjaxCaptureOptions(AjaxCaptureOptions ajaxCaptureOptions) {
      this.ajaxCaptureOptions = ajaxCaptureOptions;
    }
  }

  public static class TableHeaderRow extends BaseTableRow<TableHeaderCell> {
    /**
     * Do not use
     *
     * @param parent
     * @param cells
     */
    public TableHeaderRow(AbstractTable<?> parent, TableHeaderCell... cells) {
      super(parent, cells);
    }

    protected TableHeaderRow(ElementId parent, TableHeaderCell... cells) {
      super(parent, cells);
    }

    protected TableHeaderRow(ElementId parent, Object... cells) {
      super(parent, cells);
    }

    @Override
    protected TableHeaderCell convertCell(Object cell) {
      if (cell instanceof TableHeaderCell) {
        return (TableHeaderCell) cell;
      }
      return new TableHeaderCell(this, cell);
    }

    @Override
    protected TableHeaderCell createEmptyCell() {
      return new TableHeaderCell(this, (Object) null);
    }

    protected TableHeaderCell convertHeading(Object heading) {
      if (heading == null) {
        return new TableHeaderCell(this, new TextLabel(""));
      }

      if (heading instanceof TableHeaderCell) {
        return (TableHeaderCell) heading;
      }
      return new TableHeaderCell(this, heading);
    }
  }

  public static class TableCell extends TagState {
    private final List<Object> contents;
    private Object sortData;
    private Object filterData;
    private SortInfo sortInfo;
    private int colSpan;
    private Label title;

    public TableCell() {
      contents = Lists.newArrayList();
    }

    public TableCell(Object... contents) {
      this.contents = Lists.newArrayList(contents);
    }

    public List<Object> getContent() {
      return contents;
    }

    public TableCell addContent(Object content) {
      contents.add(content);
      return this;
    }

    public SortInfo getSortInfo() {
      return sortInfo;
    }

    public Object getSortData() {
      return sortData;
    }

    public TableCell setSortData(Object sortData) {
      this.sortData = sortData;
      if (sortData != null) {
        this.sortInfo = convertSortData(sortData);
      }
      return this;
    }

    private SortInfo convertSortData(Object data) {
      if (data instanceof Date) {
        return new SortInfo(2, ((Date) data).getTime());
      }
      if (data instanceof Number) {
        return new SortInfo(1, data);
      }
      return new SortInfo(0, data);
    }

    public static class SortInfo {
      private final int type; // 0 = string, 1 = int, 2= date
      private final Object data;

      protected SortInfo(int type, Object data) {
        this.type = type;
        this.data = data;
      }

      public int getType() {
        return type;
      }

      public Object getData() {
        return data;
      }
    }

    public Object getFilterData() {
      return filterData;
    }

    public void setFilterData(Object filterData) {
      this.filterData = filterData;
    }

    public int getColSpan() {
      return colSpan;
    }

    public void setColSpan(int colSpan) {
      this.colSpan = colSpan;
    }

    public Label getTitle() {
      return title;
    }

    public void setTitle(Label title) {
      this.title = title;
    }
  }

  public static class TableHeaderCell extends TableCell {
    private Sort sort;

    public TableHeaderCell(ElementId parent, Object... content) {
      super(content);
      setElementId(
          new AppendedElementId(
              parent, new AppendedElementId(new SimpleElementId("th"), new PageUniqueId())));
    }

    public Sort getSort() {
      return sort;
    }

    public void setSort(Sort sort) {
      this.sort = sort;
    }
  }

  public boolean isWrap() {
    return wrap;
  }

  public void setWrap(boolean wrap) {
    this.wrap = wrap;
  }

  public void makePresentation() {
    this.presentation = true;
  }

  public boolean isPresentation() {
    return presentation;
  }

  public boolean isNoColGroup() {
    return noColGroup;
  }

  public void setNoColGroup(boolean noColGroup) {
    this.noColGroup = noColGroup;
  }
}
