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

package com.tle.web.sections.standard;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.model.TableState.TableHeaderRow;
import com.tle.web.sections.standard.model.TableState.TableRow;

@NonNullByDefault
public abstract class AbstractTable<S extends TableState> extends AbstractRenderedComponent<S> {
  public enum Sort {
    PRIMARY_ASC,
    PRIMARY_DESC,
    SORTABLE_ASC,
    SORTABLE_DESC,
    NONE
  }

  @Nullable private TableHeaderRow headerRow;
  private Sort[] columnSorts;

  protected AbstractTable(String defaultRenderer) {
    super(defaultRenderer);
  }

  protected abstract S instantiateModelEx(@Nullable SectionInfo info);

  @Override
  public Object instantiateModel(@Nullable SectionInfo info) {
    S state = instantiateModelEx(info);
    if (info != null) {
      setupState(info, state);
    }
    return state;
  }

  @Override
  protected void prepareModel(RenderContext info) {
    super.prepareModel(info);

    final S state = getState(info);

    final TableHeaderRow stateHeaderRow = state.getHeaderRow();
    if (stateHeaderRow == null) {
      state.setHeaderRow(headerRow);
    }

    final Sort[] sorts = state.getColumnSorts();
    if (sorts == null) {
      state.setColumnSorts(columnSorts);
    }
  }

  public TableHeaderRow setColumnHeadings(SectionInfo info, Object... columnHeadings) {
    return getState(info).setColumnHeadings(columnHeadings);
  }

  public TableHeaderRow setColumnHeadings(Object... columnHeadings) {
    ensureBuildingTree();
    headerRow = new TableHeaderRow(this);
    for (Object heading : columnHeadings) {
      headerRow.addCell(heading);
    }
    return headerRow;
  }

  public Object[] getColumnHeadings(SectionInfo info) {
    return getState(info).getColumnHeadings();
  }

  @Nullable
  public Object[] getColumnHeadings() {
    if (headerRow == null) {
      return null;
    }
    return headerRow.getCells().toArray();
  }

  public static TableCell convertCell(@Nullable Object cell) {
    if (cell == null) {
      return new TableCell(new TextLabel("")); // $NON-NLS-1$
    }

    if (cell instanceof TableCell) {
      return (TableCell) cell;
    }
    return new TableCell(cell);
  }

  public TableRow addRow(SectionInfo info, Object... cells) {
    return getState(info).addRow(cells);
  }

  public void setColumnSorts(Sort... columnSorts) {
    ensureBuildingTree();
    // Sanity check
    validateSorts(columnSorts);

    this.columnSorts = columnSorts;
  }

  public static void validateSorts(Sort... columnSorts) {
    boolean hasPrimary = false;
    boolean hasSortables = false;
    for (Sort sort : columnSorts) {
      if (sort == Sort.NONE) {
        continue;
      }
      hasSortables = true;
      if (sort == Sort.PRIMARY_ASC || sort == Sort.PRIMARY_DESC) {
        if (hasPrimary) {
          throw new Error("A primary sort column is already given!"); // $NON-NLS-1$
        }
        hasPrimary = true;
      }
    }
    if (hasSortables && !hasPrimary) {
      throw new Error("A primary sort column was not provided"); // $NON-NLS-1$
    }
  }
}
