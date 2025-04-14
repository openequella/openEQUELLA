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

package com.tle.web.sections.standard.renderers;

import com.google.common.collect.Lists;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.model.TableState.TableHeaderCell;
import com.tle.web.sections.standard.model.TableState.TableHeaderRow;
import com.tle.web.sections.standard.model.TableState.TableRow;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("nls")
public class TableRenderer extends AbstractComponentRenderer {
  private final TableState tableState;
  protected final RendererFactory rendererFactory;

  public TableRenderer(TableState state, RendererFactory rendererFactory) {
    super(state);
    this.tableState = state;
    this.rendererFactory = rendererFactory;
  }

  @Override
  protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs)
      throws IOException {
    super.prepareFirstAttributes(writer, attrs);
    attrs.put("cellpadding", "0");
    attrs.put("cellspacing", "0");
    if (tableState.isPresentation()) {
      attrs.put("role", "presentation");
    }
  }

  @Override
  protected void writeMiddle(SectionWriter writer) throws IOException {
    final TableHeaderRow header = tableState.getHeaderRow();
    final List<TableRow> rows = tableState.getRows();

    int cols = 0;
    if (header != null) {
      cols = header.getCells().size();
    } else if (rows != null && rows.size() > 0) {
      cols = rows.get(0).getCells().size();
    }

    if (!tableState.isNoColGroup()) {
      writer.writeTag("colgroup");
      for (int i = 0; i < cols; i++) {
        writer.writeTag("col", Collections.singletonMap("class", "column" + i), true);
      }
      writer.endTag("colgroup");
    }

    if (header != null) {
      writer.write("\t");
      writer.writeTag("thead");
      writer.write("\n");
      writer.write("\t\t");
      renderHeaderRow(writer, header);
      writer.write("\t");
      writer.endTag("thead");
      writer.write("\n");
    }

    if (rows != null) {
      writer.write("\t");
      writer.writeTag("tbody");
      writer.write("\n");
      for (int i = 0; i < rows.size(); i++) {
        TableRow row = rows.get(i);
        writer.write("\t\t");
        renderRow(writer, row, i);
      }
      writer.write("\t");
      writer.endTag("tbody");
      writer.write("\n");
    }
  }

  protected void renderHeaderRow(SectionWriter writer, TableHeaderRow headerRow)
      throws IOException {
    // To Override
  }

  protected void renderHeaderCell(SectionWriter writer, TableHeaderCell cell, int columnIndex)
      throws IOException {
    TagRenderer th = new TagRenderer("th", cell);
    th.realRender(writer);
  }

  protected void renderRow(SectionWriter writer, TableRow row, int index) throws IOException {
    writer.writeTag("tr");
    List<TableCell> cells = row.getCells();
    for (int columnIndex = 0; columnIndex < cells.size(); columnIndex++) {
      TableCell cell = cells.get(columnIndex);
      renderCell(writer, cell, columnIndex);
    }
    writer.endTag("tr");
  }

  protected void renderCell(SectionWriter writer, TableCell cell, int columnIndex)
      throws IOException {
    TagRenderer td = new TagRenderer("td", cell);

    final List<Object> contents = cell.getContent();
    final List<SectionRenderable> renderables = Lists.newArrayList();
    for (Object content : contents) {
      renderables.add(rendererFactory.convertToRenderer(content));
    }
    td.setNestedRenderable(CombinedRenderer.combineMultipleResults(renderables));
    td.realRender(writer);
  }

  @Override
  protected String getTag() {
    return "table";
  }

  protected TableState getTableState() {
    return tableState;
  }
}
