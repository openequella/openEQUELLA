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

import java.util.Arrays;
import java.util.List;

import com.tle.common.Check;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.standard.js.JSMutableListComponent;
import com.tle.web.sections.standard.js.impl.DelayedJSMutableListComponent;
import com.tle.web.sections.standard.js.impl.DelayedJSValueComponent;
import com.tle.web.sections.standard.model.HtmlMutableListState;
import com.tle.web.sections.standard.model.MutableListModel;
import com.tle.web.sections.standard.model.Option;

public class MutableList<T>
    extends AbstractValueComponent<HtmlMutableListState, JSMutableListComponent>
    implements ParametersEventListener, BookmarkEventListener, JSMutableListComponent {
  private MutableListModel<T> listModel;
  protected DelayedJSMutableListComponent delayedList;

  public MutableList() {
    super(RendererConstants.DROPDOWN);
  }

  @Override
  public Class<HtmlMutableListState> getModelClass() {
    return HtmlMutableListState.class;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void prepareModel(RenderContext info) {
    super.prepareModel(info);
    List<Option<T>> options = listModel.getOptions(info);
    getState(info).setOptions((List) options);
  }

  @Override
  protected DelayedJSValueComponent<JSMutableListComponent> createDelayedJS(ElementId id) {
    delayedList = new DelayedJSMutableListComponent(this);
    return delayedList;
  }

  @Override
  public void bookmark(SectionInfo info, BookmarkEvent event) {
    if (addToThisBookmark(info, event)) {
      List<String> vals = listModel.getValues(info);
      if (!Check.isEmpty(vals)) {
        event.setParams(getParameterId(), vals);
      }
    }
  }

  @Override
  public void handleParameters(SectionInfo info, ParametersEvent event) {
    String[] params = event.getParameterValues(getParameterId());
    if (params != null) {
      listModel.setValues(info, Arrays.asList(params));
    }
  }

  public MutableListModel<T> getListModel() {
    return listModel;
  }

  public void setListModel(MutableListModel<T> listModel) {
    this.listModel = listModel;
  }

  public List<String> getValues(SectionInfo info) {
    return listModel.getValues(info);
  }

  public void setValues(SectionInfo info, List<String> strings) {
    this.listModel.setValues(info, strings);
  }

  public boolean isEmpty(SectionInfo info) {
    return listModel.isEmpty(info);
  }

  @Override
  public JSCallable createAddFunction() {
    return delayedList.createAddFunction();
  }

  @Override
  public JSCallable createRemoveFunction() {
    return delayedList.createRemoveFunction();
  }

  @Override
  public JSExpression createGetNameExpression() {
    return delayedList.createGetNameExpression();
  }

  @Override
  public JSExpression createNotEmptyExpression() {
    return delayedList.createNotEmptyExpression();
  }

  @SuppressWarnings("nls")
  @Override
  public void document(SectionInfo info, DocumentParamsEvent event) {
    addDocumentedParam(
        event, getParameterId(), List.class.getName() + " of " + String.class.getName());
  }

  @Override
  public JSCallable createSetAllFunction() {
    return delayedList.createSetAllFunction();
  }
}
