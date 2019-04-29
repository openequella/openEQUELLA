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

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.standard.js.JSValueComponent;
import com.tle.web.sections.standard.model.HtmlNumberFieldState;
import com.tle.web.sections.standard.model.HtmlValueState;

public class NumberField
    extends AbstractValueStateComponent<HtmlNumberFieldState, JSValueComponent> {
  private boolean dontBookmarkBlank = true;
  private Number min;
  private Number max;
  private Number step;
  private Number defaultNumber;
  private boolean anyStep;
  private boolean integersOnly = true;

  public NumberField() {
    super(RendererConstants.NUMBERFIELD);
  }

  @Override
  public Object instantiateModel(SectionInfo info) {
    return setupState(info, new HtmlNumberFieldState());
  }

  public Number getValue(SectionInfo info) {
    if (integersOnly) {
      return getIntValue(info);
    }
    HtmlValueState state = getState(info);
    return Double.parseDouble(state.getValue());
  }

  public void setValue(SectionInfo info, Number value) {
    HtmlValueState state = getState(info);
    state.setValue(String.valueOf(value));
  }

  // You should still be able to set the value to invalid numbers. E.g. to
  // display user entered text on an error validation screen.
  public void setStringValue(SectionInfo info, String value) {
    HtmlValueState state = getState(info);
    state.setValue(value);
  }

  @Override
  protected HtmlNumberFieldState setupState(SectionInfo info, HtmlNumberFieldState state) {
    super.setupState(info, state);
    if (min != null) {
      state.setMin(min);
    }
    if (max != null) {
      state.setMax(max);
    }
    if (step != null) {
      state.setStep(step);
    }
    if (defaultNumber != null) {
      state.setValue(defaultNumber.toString());
    }
    state.setAnyStep(anyStep);
    return state;
  }

  @Override
  public void document(SectionInfo info, DocumentParamsEvent event) {
    addDocumentedParam(event, getParameterId(), min + "-" + max);
  }

  @Override
  protected String getBookmarkStringValue(HtmlNumberFieldState state) {
    String value = state.getValue();
    if (value != null && dontBookmarkBlank && value.isEmpty()) {
      return null;
    }
    return value;
  }

  public void setDontBookmarkBlank(boolean b) {
    ensureBuildingTree();
    this.dontBookmarkBlank = b;
  }

  public Number getMin() {
    return min;
  }

  public void setMin(Number min) {
    ensureBuildingTree();
    this.min = min;
  }

  public Number getMax() {
    return max;
  }

  public void setMax(Number max) {
    ensureBuildingTree();
    this.max = max;
  }

  public Number getStep() {
    return step;
  }

  public void setStep(Number step) {
    ensureBuildingTree();
    this.step = step;
  }

  public Number getDefaultNumber() {
    return defaultNumber;
  }

  public void setDefaultNumber(Number defaultNumber) {
    ensureBuildingTree();
    this.defaultNumber = defaultNumber;
  }

  public boolean isAnyStep() {
    return anyStep;
  }

  public void setAnyStep(boolean anyStep) {
    ensureBuildingTree();
    this.anyStep = anyStep;
  }

  public boolean isIntegersOnly() {
    return integersOnly;
  }

  public void setIntegersOnly(boolean integersOnly) {
    ensureBuildingTree();
    this.integersOnly = integersOnly;
  }
}
