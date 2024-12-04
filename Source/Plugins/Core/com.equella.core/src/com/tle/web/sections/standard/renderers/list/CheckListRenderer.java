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

package com.tle.web.sections.standard.renderers.list;

import com.tle.common.i18n.CurrentLocale;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.js.JSListComponent;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.AbstractComponentRenderer;
import com.tle.web.sections.standard.renderers.FormValuesLibrary;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CheckListRenderer extends AbstractComponentRenderer
    implements JSListComponent, JSDisableable {
  private boolean asList;
  private boolean orderedList = false;
  private boolean showBulkOps;
  private final boolean multiple;
  private final List<Option<?>> options;
  private final Set<String> selectedSet;

  private static final PluginResourceHelper urlHelper =
      ResourcesService.getResourceHelper(CheckListRenderer.class);

  public CheckListRenderer(HtmlListState listState) {
    super(listState);
    options = listState.getOptions();
    selectedSet = listState.getSelectedValues();
    multiple = listState.isMultiple();
  }

  public void setAsList(boolean asList) {
    this.asList = asList;
  }

  public void setShowBulkOps(boolean showBulkOps) {
    this.showBulkOps = showBulkOps;
  }

  @Override
  protected void processHandler(
      SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler) {
    if (!event.equals(JSHandler.EVENT_CHANGE)) {
      super.processHandler(writer, attrs, event, handler);
    }
  }

  @Override
  public void preRender(PreRenderContext info) {
    super.preRender(info);
    info.addCss(urlHelper.url("css/checklist.css")); // $NON-NLS-1$
  }

  @SuppressWarnings({"nls"})
  @Override
  protected void writeMiddle(SectionWriter writer) throws IOException {
    JSHandler lsChangeHandler = state.getHandler(JSHandler.EVENT_CHANGE);
    JSHandler changeHandler = null;
    if (lsChangeHandler != null) {
      changeHandler =
          new StatementHandler(new SimpleFunction(JSHandler.EVENT_CHANGE, this, lsChangeHandler));
    }
    String baseId = state.getName();

    Map<String, String> attrs = new LinkedHashMap<String, String>();
    attrs.put("type", multiple ? "checkbox" : "radio");
    attrs.put("name", baseId);
    int i = 0;
    for (Option<?> option : options) {
      if (asList) {
        // Add a odd|even class to the LI, to enable zebra mode,
        // if the prevailing stylesheet so requires
        if (i % 2 == 0) {
          writer.writeTag("li", "class", "even");
        } else {
          writer.writeTag("li", "class", "odd");
        }
      }
      String val = option.getValue();

      String checkId = baseId + "_" + i;
      attrs.put("id", checkId);
      attrs.put("value", val);
      if (selectedSet.contains(val)) {
        attrs.put("checked", "checked");
      } else {
        attrs.remove("checked");
      }

      if (option.isDisabled()) {
        attrs.put("disabled", "true");
      } else {
        attrs.remove("disabled");
      }
      if (changeHandler != null) {
        writer.bindHandler(JSHandler.EVENT_CLICK, attrs, changeHandler);
      }

      writer.writeTag("input", attrs);
      Map<String, String> labelAttrs = new HashMap<String, String>();
      labelAttrs.put("for", checkId);
      if (option.hasAltTitleAttr()) {
        labelAttrs.put("title", option.getAltTitleAttr());
      }
      writer.writeTag("label", labelAttrs);
      if (option.isNameHtml()) {
        writer.write(option.getName());
      } else {
        writer.writeText(option.getName());
      }
      writer.endTag("label");
      writer.write('\n');
      i++;
      if (asList) {
        writer.endTag("li");
      }
    }
  }

  @Override
  @SuppressWarnings("nls")
  protected void writeEnd(SectionWriter writer) throws IOException {
    super.writeEnd(writer);

    if (showBulkOps) {
      final String elemId = state.getElementId(writer);

      writer.writeTag("div", "class", "checklist_bulkops");
      createBulkCheckLink(elemId, "checkall", "true").realRender(writer);
      writer.writeText(" | ");
      createBulkCheckLink(elemId, "uncheckall", "false").realRender(writer);
      writer.writeText(" | ");
      createBulkCheckLink(elemId, "invert", "undefined").realRender(writer);
      writer.endTag("div");
    }
  }

  @SuppressWarnings("nls")
  private LinkRenderer createBulkCheckLink(String elementId, String labelKey, String checkState) {
    HtmlLinkState state = new HtmlLinkState();
    state.setLabel(
        new TextLabel(
            CurrentLocale.get("com.tle.web.sections.standard.renderers.checklist." + labelKey)));
    state.setClickHandler(
        new OverrideHandler(
            new ScriptStatement(
                "setAllCheckedState(" + JSUtils.toJSString(elementId) + "," + checkState + ");")));
    return new LinkRenderer(state);
  }

  @Override
  protected void prepareLastAttributes(SectionWriter writer, Map<String, String> attrs) {
    super.prepareLastAttributes(writer, attrs);
    addClass(attrs, "checklist"); // $NON-NLS-1$
  }

  @Override
  protected String getTag() {
    return asList
        ? isOrderedList() ? "ol" : "ul"
        : "div"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  @Override
  public JSExpression createNotEmptyExpression() {
    return new FunctionCallExpression(
        FormValuesLibrary.IS_SOME_CHECKED, new StringExpression(state.getName()));
  }

  @Override
  public JSExpression createGetNameExpression() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JSExpression createGetExpression() {
    return new FunctionCallExpression(
        multiple ? FormValuesLibrary.GET_CHECKED_VALUES : FormValuesLibrary.GET_CHECK_VALUE,
        new StringExpression(state.getName()));
  }

  @Override
  public JSCallable createSetFunction() {
    throw new UnsupportedOperationException("Not yet"); // $NON-NLS-1$
  }

  @Override
  public JSCallable createResetFunction() {
    throw new UnsupportedOperationException("Not yet"); // $NON-NLS-1$
  }

  @Override
  public JSCallable createDisableFunction() {
    return new PrependedParameterFunction(
        FormValuesLibrary.SET_ALL_DISABLED_STATE, new StringExpression(state.getName()));
  }

  public void setOrderedList(boolean orderedList) {
    this.orderedList = orderedList;
  }

  public boolean isOrderedList() {
    return orderedList;
  }

  @Override
  public JSCallable createSetAllFunction() {
    throw new UnsupportedOperationException("Not yet"); // $NON-NLS-1$
  }
}
