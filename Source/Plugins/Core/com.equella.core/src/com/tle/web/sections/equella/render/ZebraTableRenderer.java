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

package com.tle.web.sections.equella.render;

import com.google.common.collect.Lists;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.ajax.AjaxCaptureOptions;
import com.tle.web.sections.ajax.AjaxTagRenderer;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.js.StandardExpressions;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQueryTextFieldHint;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.NotExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;
import com.tle.web.sections.js.generic.statement.IfElseStatement;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.js.JSTableComponent;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlValueState;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.BaseTableRow;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.model.TableState.TableCell.SortInfo;
import com.tle.web.sections.standard.model.TableState.TableHeaderCell;
import com.tle.web.sections.standard.model.TableState.TableHeaderRow;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.SpanRenderer;
import com.tle.web.sections.standard.renderers.TableRenderer;
import com.tle.web.sections.standard.renderers.TextFieldRenderer;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@SuppressWarnings("nls")
public class ZebraTableRenderer extends TableRenderer implements JSTableComponent {
  private static final int DEFAULT_FILTER_THRESHOLD = 10;

  @PlugKey("renderer.table.zebra.sort")
  private static Label LABEL_SORT;

  @PlugKey("renderer.table.zebra.filter")
  private static Label LABEL_FILTER;

  @PlugURL("scripts/component/zebratable.js")
  private static String URL_JS;

  @PlugURL("css/component/zebratable.css")
  private static String URL_CSS;

  static {
    PluginResourceHandler.init(ZebraTableRenderer.class);
  }

  public static final CssInclude CSS = CssInclude.include(URL_CSS).hasRtl().make();
  private static final IncludeFile INCLUDE =
      new IncludeFile(URL_JS, StandardExpressions.STANDARD_JS);

  private static final ExternallyDefinedFunction SORT_FUNCTION =
      new ExternallyDefinedFunction("sortColumn", 4, INCLUDE);
  private static final ExternallyDefinedFunction FILTER_FUNCTION =
      new ExternallyDefinedFunction("filterTable", 2, INCLUDE);

  private static final ExternallyDefinedFunction SWALLOW_ENTER_FUNCTION =
      new ExternallyDefinedFunction("swallowEnter", 1, INCLUDE);

  protected int filterThreshold = -1;
  protected boolean wrap = false;

  public ZebraTableRenderer(TableState state, RendererFactory rendererFactory) {
    super(state, rendererFactory);
    this.wrap = state.isWrap();
    addClass("zebra");
  }

  @Override
  public void realRender(SectionWriter writer) throws IOException {
    final TableState tableState = getTableState();

    if (wrap) {
      writer.writeTag("div", "class", "tableContainer");
    }

    int filter = (filterThreshold != -1 ? filterThreshold : tableState.getFilterThreshold());
    if (filter == -1) {
      filter = DEFAULT_FILTER_THRESHOLD;
    }

    if (tableState.isFilterable() && tableState.getRows().size() >= filter) {
      addClass("withfilter");

      final HtmlComponentState divState = new HtmlComponentState();
      divState.addClass("tableFilter");
      final DivRenderer div = new DivRenderer(divState);

      final HtmlValueState ts = new HtmlValueState();
      ts.setElementId(new AppendedElementId(state, "fil"));
      ts.addClass("filterBox");

      IfElseStatement ife =
          new IfElseStatement(
              new NotExpression(Jq.methodCall(ts, Js.function("hasClass"), "blur")),
              Js.call_s(FILTER_FUNCTION, Jq.$(state), Jq.$val(ts)),
              Js.call_s(FILTER_FUNCTION, Jq.$(state), Js.str("")));

      JSExpression boundKeyup = Js.methodCall(Jq.$(ts), Js.function("keyup"), Js.function(ife));

      ScriptVariable event = Js.var("event");

      JSExpression boundKeydownAndKeyup =
          Js.methodCall(
              boundKeyup,
              Js.function("keydown"),
              Js.function(Js.call_s(SWALLOW_ENTER_FUNCTION, event), event));

      ts.addReadyStatements(Js.statement(boundKeydownAndKeyup));
      ts.addTagProcessor(new JQueryTextFieldHint(LABEL_FILTER, ts));

      final TextFieldRenderer textFieldRenderer = new TextFieldRenderer(ts);
      div.setNestedRenderable(textFieldRenderer);
      div.preRender(writer);
      div.realRender(writer);
    }

    super.realRender(writer);

    if (wrap) {
      writer.endTag("div");
    }
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(CSS);

    final TableState state = getTableState();
    final TableHeaderRow headerRow = state.getHeaderRow();

    // Sort info
    final Sort[] sorts = state.getColumnSorts();
    if (sorts != null && headerRow != null) {
      // This *should* throw an 'index out of bounds' when appropriate
      List<TableHeaderCell> cells = headerRow.getCells();
      for (int i = 0; i < sorts.length; i++) {
        cells.get(i).setSort(sorts[i]);
      }
    }

    // Initial sort javascript
    if (!info.getBooleanAttribute(state)) {
      TableHeaderCell primarySortColumn = null;
      int primarySortColumnIndex = -1;

      if (headerRow != null) {
        final List<TableHeaderCell> cells = headerRow.getCells();
        for (int i = 0; i < cells.size(); i++) {
          final TableHeaderCell cell = cells.get(i);
          final Sort sort = cell.getSort();
          if (sort == Sort.PRIMARY_ASC || sort == Sort.PRIMARY_DESC) {
            primarySortColumn = cell;
            primarySortColumnIndex = i;
          }
        }

        if (primarySortColumn != null) {
          state.addReadyStatements(
              Js.call_s(
                  SORT_FUNCTION,
                  Jq.$(state),
                  Jq.$(primarySortColumn),
                  primarySortColumnIndex,
                  true));
        }
      }

      info.setAttribute(state, true);
    }

    /* FIXME: should not need to create temporary renderers for preRendering */

    if (headerRow != null) {
      new ZebraHeaderRowRenderer(state, headerRow, 0).preRender(info);
    }

    final List<TableRow> rows = state.getRows();
    for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
      new ZebraRowRenderer(state, rows.get(rowIndex), rowIndex).preRender(info);
    }

    super.preRender(info);
  }

  @Override
  protected void renderHeaderRow(SectionWriter writer, TableHeaderRow headerRow)
      throws IOException {
    ZebraHeaderRowRenderer tr = new ZebraHeaderRowRenderer(getTableState(), headerRow, 0);
    tr.realRender(writer);
  }

  @Override
  protected void renderRow(SectionWriter writer, TableRow row, int index) throws IOException {
    ZebraRowRenderer tr = new ZebraRowRenderer(getTableState(), row, index);
    tr.realRender(writer);
  }

  @Override
  protected void renderHeaderCell(SectionWriter writer, TableHeaderCell cell, int columnIndex)
      throws IOException {
    ZebraHeaderCellRenderer th = new ZebraHeaderCellRenderer(getTableState(), cell, columnIndex);
    th.realRender(writer);
  }

  @Override
  protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException {
    super.writeStart(writer, attrs);
    writer.write("\n");
  }

  @Override
  protected void writeMiddle(SectionWriter writer) throws IOException {
    super.writeMiddle(writer);
    writer.write("\n");
  }

  @Override
  protected void writeEnd(SectionWriter writer) throws IOException {
    super.writeEnd(writer);
    writer.write("\n");
  }

  public int getFilterThreshold() {
    return filterThreshold;
  }

  public boolean getWrap() {
    return wrap;
  }

  public void setFilterThreshold(int filterThreshold) {
    this.filterThreshold = filterThreshold;
  }

  public void setWrap(boolean wrap) {
    this.wrap = wrap;
  }

  @SuppressWarnings("unused")
  private abstract class BaseZebraRowRenderer<R extends BaseTableRow<C>, C extends TableCell>
      extends AjaxTagRenderer {
    protected final TableState tableState;
    protected final R rowState;
    protected final int rowIndex;

    protected BaseZebraRowRenderer(TableState tableState, R state, int rowIndex) {
      super("tr", state, null, true);
      this.tableState = tableState;
      this.rowState = state;
      this.rowIndex = rowIndex;
      addClass(rowIndex % 2 == 0 ? "even" : "odd");
    }

    @Override
    public SectionRenderable getNestedRenderable() {
      SectionRenderable result = null;
      final List<C> cells = rowState.getCells();
      for (int columnIndex = 0; columnIndex < cells.size(); columnIndex++) {
        result =
            CombinedRenderer.combineMultipleResults(
                result, createCellRenderer(cells.get(columnIndex), columnIndex));
      }
      return result;
    }

    @Override
    protected boolean isCapture(SectionWriter writer) {
      return false;
    }

    @Override
    protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException {
      super.writeStart(writer, attrs);
      writer.write("\n");
    }

    @Override
    protected void writeMiddle(SectionWriter writer) throws IOException {
      super.writeMiddle(writer);
      writer.write("\n");
    }

    @Override
    protected void writeEnd(SectionWriter writer) throws IOException {
      super.writeEnd(writer);
      writer.write("\n");
    }

    protected abstract SectionRenderable createCellRenderer(C cell, int columnIndex);
  }

  private class ZebraRowRenderer extends BaseZebraRowRenderer<TableRow, TableCell> {
    private final AjaxCaptureOptions ajaxCaptureOptions;

    public ZebraRowRenderer(TableState tableState, TableRow state, int rowIndex) {
      super(tableState, state, rowIndex);
      addClass("rowShown");
      ajaxCaptureOptions = state.getAjaxCaptureOptions();
    }

    @Override
    protected boolean isCapture(SectionWriter writer) {
      return ajaxCaptureOptions != null;
    }

    @Override
    protected boolean isCollection(RenderContext context) {
      return ajaxCaptureOptions.isCollection();
    }

    @Override
    protected String getAjaxDivId(RenderContext context) {
      return ajaxCaptureOptions.getAjaxId();
    }

    @Override
    protected Map<String, Object> getCaptureParams(RenderContext context) {
      return ajaxCaptureOptions.getParams();
    }

    @Override
    protected SectionRenderable createCellRenderer(TableCell cell, int columnIndex) {
      return new ZebraCellRenderer(tableState, cell, columnIndex);
    }
  }

  private class ZebraHeaderRowRenderer
      extends BaseZebraRowRenderer<TableHeaderRow, TableHeaderCell> {
    public ZebraHeaderRowRenderer(TableState tableState, TableHeaderRow state, int rowIndex) {
      super(tableState, state, rowIndex);
    }

    @Override
    protected SectionRenderable createCellRenderer(TableHeaderCell cell, int columnIndex) {
      return new ZebraHeaderCellRenderer(tableState, cell, columnIndex);
    }
  }

  @SuppressWarnings("unused")
  private class ZebraCellRenderer extends TagRenderer {
    protected final TableState tableState;
    protected final TableCell cellState;
    protected final int columnIndex;

    public ZebraCellRenderer(TableState tableState, TableCell state, int columnIndex) {
      this("td", tableState, state, columnIndex);
    }

    protected ZebraCellRenderer(
        String tag, TableState tableState, TableCell state, int columnIndex) {
      super(tag, state);
      this.tableState = tableState;
      this.cellState = state;
      this.columnIndex = columnIndex;
    }

    @Override
    protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs)
        throws IOException {
      if (cellState.getColSpan() > 0) {
        attrs.put("colspan", Integer.toString(cellState.getColSpan()));
      }
      if (cellState.getTitle() != null) {
        attrs.put("title", cellState.getTitle().getText());
      }
    }

    @Override
    public SectionRenderable getNestedRenderable() {
      final SortInfo sortInfo = cellState.getSortInfo();
      final Object filterData = cellState.getFilterData();

      SectionRenderable content = getCellContentRenderable();

      if (sortInfo != null) {
        final HtmlComponentState sortDataState = new HtmlComponentState();
        final String sortClass;
        switch (sortInfo.getType()) {
          case 1:
            sortClass = "sortInteger";
            break;
          case 2:
            sortClass = "sortDate";
            break;
          default:
            sortClass = "sortString";
        }
        sortDataState.addClass("hidden sortData " + sortClass);
        final SpanRenderer sortDataRenderer = new SpanRenderer(sortDataState);
        sortDataRenderer.setNestedRenderable(rendererFactory.convertToRenderer(sortInfo.getData()));

        content = CombinedRenderer.combineMultipleResults(sortDataRenderer, content);
      }

      if (filterData != null) {
        final SpanRenderer filterDataRenderer = new SpanRenderer(filterData);
        filterDataRenderer.addClass("hidden filterData");
        content =
            CombinedRenderer.combineMultipleResults(
                content, SectionUtils.convertToRenderer(filterDataRenderer));
      }

      return content;
    }

    private SectionRenderable getCellContentRenderable() {
      final List<Object> contents = cellState.getContent();
      final List<SectionRenderable> renderables = Lists.newArrayList();
      for (Object content : contents) {
        renderables.add(rendererFactory.convertToRenderer(content));
      }
      return CombinedRenderer.combineMultipleResults(renderables);
    }

    @Override
    protected void writeEnd(SectionWriter writer) throws IOException {
      super.writeEnd(writer);
      writer.write("\n");
    }
  }

  private class ZebraHeaderCellRenderer extends ZebraCellRenderer {
    private final TableHeaderCell headerCellState;
    private final boolean sortable;

    public ZebraHeaderCellRenderer(TableState tableState, TableHeaderCell state, int columnIndex) {
      super("th", tableState, state, columnIndex);
      this.headerCellState = state;

      final Sort sort = headerCellState.getSort();
      sortable = sort != null && sort != Sort.NONE;
      if (sortable) {
        headerCellState.addClass("sortable");
        if (sort == Sort.PRIMARY_ASC || sort == Sort.SORTABLE_ASC) {
          addClass("sortAsc");
        } else if (sort == Sort.PRIMARY_DESC || sort == Sort.SORTABLE_DESC) {
          addClass("sortDesc");
        }
        headerCellState.setClickHandler(
            Js.handler(
                Js.call_s(SORT_FUNCTION, Jq.$(tableState), Jq.$(cellState), columnIndex, false)));
      }
    }

    @Override
    protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs)
        throws IOException {
      super.prepareFirstAttributes(writer, attrs);

      if (sortable) {
        attrs.put("title", LABEL_SORT.getText());
      }
    }

    @Override
    public SectionRenderable getNestedRenderable() {
      if (sortable) {
        DivRenderer arrow = new DivRenderer("sortArrow", "");
        return CombinedRenderer.combineMultipleResults(super.getNestedRenderable(), arrow);
      }
      return super.getNestedRenderable();
    }
  }

  @Override
  public JSCallable createFilterFunction() {
    return new PrependedParameterFunction(FILTER_FUNCTION, Jq.$(this));
  }
}
