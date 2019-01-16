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

package com.tle.web.sections.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxEventCreator.SimpleResult;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.ajax.handler.AnonymousAjaxCallback;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSValidator;
import com.tle.web.sections.js.generic.expression.ElementByIdExpression;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.js.validators.SimpleValidator;
import com.tle.web.sections.standard.event.ValueSetListener;
import com.tle.web.sections.standard.js.JSListComponent;
import com.tle.web.sections.standard.js.impl.DelayedJSListComponent;
import com.tle.web.sections.standard.js.impl.DelayedJSValueComponent;
import com.tle.web.sections.standard.model.HtmlListModel;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.NothingListModel;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.SelectOptionsCallback;
import com.tle.web.sections.standard.renderers.list.DropDownRenderer;

/**
 * A multi selection list component.
 *
 * <p>It provides methods for getting and setting a list of values, as strings or the values stored
 * in the model. <br>
 * The default renderer is usually {@link DropDownRenderer}.
 *
 * @author jmaginnis
 */
public class MultiSelectionList<T> extends AbstractValueComponent<HtmlListState, JSListComponent>
    implements ParametersEventListener, BookmarkEventListener, JSListComponent {
  protected HtmlListModel<T> listModel = new NothingListModel<T>();
  private boolean alwaysSelect;
  private boolean alwaysSelectForBookmark;
  @AjaxFactory private AjaxGenerator ajaxMethods;
  private DelayedJSListComponent<JSListComponent> delayedList;
  private ValueSetListener<Set<String>> listener;

  public MultiSelectionList() {
    super(RendererConstants.DROPDOWN);
  }

  @Override
  protected DelayedJSValueComponent<JSListComponent> createDelayedJS(ElementId id) {
    delayedList = new DelayedJSListComponent<JSListComponent>(this);
    return delayedList;
  }

  @Override
  public Class<HtmlListState> getModelClass() {
    return HtmlListState.class;
  }

  public boolean isAlwaysSelect() {
    return alwaysSelect;
  }

  public void setAlwaysSelect(boolean alwaysSelect) {
    this.alwaysSelect = alwaysSelect;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void prepareModel(RenderContext info) {
    super.prepareModel(info);
    ensureSelection(info);
    final HtmlListState state = getState(info);
    state.setOptions((List<Option<?>>) getOptions(info));
    extraHtmlRender(info);
  }

  protected List<?> getOptions(SectionInfo info) {
    return getListModel().getOptions(info);
  }

  protected void extraHtmlRender(SectionInfo info) {
    HtmlListState state = getState(info);
    state.setMultiple(!state.isDisallowMultiple());
  }

  @Override
  public void handleParameters(SectionInfo info, ParametersEvent event) {
    handleListParameters(info, event);
  }

  protected void handleListParameters(SectionInfo info, ParametersEvent event) {
    String[] params = event.getParameterValues(getParameterId());
    if (params != null) {
      List<String> paramList = Arrays.asList(params);
      setValuesInternal(info, new HashSet<String>(paramList));
    }
  }

  public void addClass(SectionInfo info, String styleClass) {
    HtmlListState state = getState(info);
    state.addClass(styleClass);
  }

  public void setSelectedStringValue(SectionInfo info, String value) {
    if (value == null) {
      setSelectedStringValues(info, new HashSet<String>());
    } else {
      setSelectedStringValues(info, Collections.singleton(value));
    }
  }

  public void setSelectedStringValues(SectionInfo info, Collection<String> values) {
    if (values == null || values.isEmpty()) {
      Set<String> emptySet = Collections.emptySet();
      setValuesInternal(info, emptySet);
    } else {
      setValuesInternal(info, new HashSet<String>(values));
    }
  }

  public List<T> getSelectedValues(SectionInfo info) {
    Set<String> selectedValues = ensureSelection(info);
    if (selectedValues != null) {
      return listModel.getValues(info, selectedValues);
    }
    return Collections.emptyList();
  }

  protected Set<String> ensureSelection(SectionInfo info) {
    HtmlListState state = getState(info);
    Set<String> selectedValues = state.getSelectedValues();
    boolean set = false;
    if (selectedValues == null) {
      selectedValues = Collections.emptySet();
      set = true;
    }
    if (alwaysSelect && selectedValues.isEmpty()) {
      String defaultValue = listModel.getDefaultValue(info);
      if (defaultValue != null) {
        selectedValues = Collections.singleton(defaultValue);
        set = true;
      }
    }
    if (set) {
      setValuesInternal(info, selectedValues);
    }
    return selectedValues;
  }

  protected void setValuesInternal(SectionInfo info, Set<String> values) {
    getState(info).setSelectedValues(values);
    if (listener != null) {
      listener.valueSet(info, values);
    }
  }

  public Set<String> getSelectedValuesAsStrings(SectionInfo info) {
    return ensureSelection(info);
  }

  public HtmlListModel<T> getListModel() {
    return listModel;
  }

  public void setListModel(HtmlListModel<T> listModel) {
    this.listModel = listModel;
  }

  public int size(SectionInfo info) {
    return getListModel().getOptions(info).size();
  }

  @Override
  public void bookmark(SectionInfo info, BookmarkEvent event) {
    if (addToThisBookmark(info, event)) {
      Set<String> vals;
      if (alwaysSelectForBookmark) {
        vals = ensureSelection(info);
      } else {
        vals = getState(info).getSelectedValues();
      }
      event.setParams(getParameterId(), vals);
    }
  }

  public AnonymousFunction getDefaultAjaxCall() {
    JSCallAndReference callbackVar = new ExternallyDefinedFunction("callback"); // $NON-NLS-1$
    FunctionCallStatement justUpdate =
        new FunctionCallStatement(
            getDefaultAjaxUpdateCallback(), AjaxGenerator.RESULTS_VAR, AjaxGenerator.STATUS_VAR);
    FunctionCallStatement callbackStatement = new FunctionCallStatement(callbackVar);
    FunctionCallStatement postAjax =
        new FunctionCallStatement(
            getAjaxOptionsFunction(),
            new AnonymousAjaxCallback(StatementBlock.get(justUpdate, callbackStatement)));
    return new AnonymousFunction(postAjax, callbackVar);
  }

  public JSCallable getDefaultAjaxUpdateCallback() {
    return new SelectOptionsCallback(getParameterId(), new ElementByIdExpression(this));
  }

  public JSCallable getAjaxOptionsFunction() {
    return ajaxMethods.getAjaxFunction("ajaxOptions"); // $NON-NLS-1$
  }

  @AjaxMethod
  public SimpleResult ajaxOptions(SectionInfo info) {
    return new SimpleResult(getOptions(info));
  }

  public JSValidator createEmptyValidator() {
    return new SimpleValidator(delayedList.createNotEmptyExpression());
  }

  @Override
  public JSExpression createGetNameExpression() {
    return delayedList.createGetNameExpression();
  }

  @Override
  public JSExpression createNotEmptyExpression() {
    return delayedList.createNotEmptyExpression();
  }

  public void setValueSetListener(ValueSetListener<Set<String>> listener) {
    this.listener = listener;
  }

  public boolean isAlwaysSelectForBookmark() {
    return alwaysSelectForBookmark;
  }

  public void setAlwaysSelectForBookmark(boolean alwaysSelectForBookmark) {
    this.alwaysSelectForBookmark = alwaysSelectForBookmark;
  }

  @SuppressWarnings("nls")
  @Override
  public void document(SectionInfo info, DocumentParamsEvent event) {
    List<String> values = Collections.singletonList("Probably dynamic");
    try {
      List<Option<T>> options = (List<Option<T>>) getOptions(info);
      values = new ArrayList<String>();
      String defaultVal = listModel.getDefaultValue(info);
      for (Option<T> option : options) {
        String value = option.getValue();
        if (defaultVal != null && defaultVal.equals(value)) {
          value = "(" + value + ")";
        }
        values.add(value);
      }
    } catch (Exception e) {
      // nothing
    }
    String[] vals = values.toArray(new String[values.size()]);
    addDocumentedParam(event, getParameterId(), getValueType(), vals);
  }

  @SuppressWarnings("nls")
  protected String getValueType() {
    return Set.class.getName() + " of " + String.class.getName();
  }

  @Override
  public JSCallable createSetAllFunction() {
    return delayedList.createSetAllFunction();
  }
}
