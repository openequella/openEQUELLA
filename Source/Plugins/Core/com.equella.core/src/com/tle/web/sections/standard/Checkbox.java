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
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.standard.js.JSValueComponent;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.renderers.toggle.CheckboxRenderer;

/**
 * A simple boolean checkbox.
 *
 * <p>This component supplies {@link #isChecked(SectionInfo)} and {@link #setChecked(SectionInfo,
 * boolean)}. The default renderer is usually {@link CheckboxRenderer}.
 *
 * @author jmaginnis
 */
public class Checkbox extends AbstractValueComponent<HtmlBooleanState, JSValueComponent>
    implements ParametersEventListener, BookmarkEventListener {
  private static final String CHECKED_VALUE = "checked"; // $NON-NLS-1$

  public Checkbox() {
    super(RendererConstants.CHECKBOX);
  }

  @Override
  protected void prepareModel(RenderContext info) {
    super.prepareModel(info);
    getState(info).setValue(CHECKED_VALUE);
  }

  public void setChecked(SectionInfo info, boolean checked) {
    HtmlBooleanState state = getState(info);
    state.setChecked(checked);
  }

  public boolean isChecked(SectionInfo info) {
    HtmlBooleanState state = getState(info);
    return state.isChecked();
  }

  @Override
  public Class<HtmlBooleanState> getModelClass() {
    return HtmlBooleanState.class;
  }

  @Override
  public void handleParameters(SectionInfo info, ParametersEvent event) {
    String param = event.getParameter(getParameterId(), false);
    HtmlBooleanState state = getState(info);
    if (param != null && param.equals(CHECKED_VALUE)) {
      state.setChecked(true);
    }
  }

  @Override
  public void bookmark(SectionInfo info, BookmarkEvent event) {
    if (addToThisBookmark(info, event)) {
      HtmlBooleanState state = getState(info);
      if (state.isChecked()) {
        event.setParam(getParameterId(), CHECKED_VALUE);
      }
    }
  }

  @Override
  public void document(SectionInfo info, DocumentParamsEvent event) {
    addDocumentedParam(event, getParameterId(), Boolean.TYPE.getName(), CHECKED_VALUE);
  }
}
