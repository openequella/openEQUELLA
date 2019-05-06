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

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.render.HiddenInput;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.js.JSValueComponent;
import com.tle.web.sections.standard.renderers.FormValuesLibrary;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@NonNullByDefault
public class HiddenState extends AbstractHtmlComponent<HiddenState.HiddenStateModel>
    implements HtmlRenderer, BookmarkEventListener, ParametersEventListener, JSValueComponent {
  @Override
  public Class<HiddenStateModel> getModelClass() {
    return HiddenStateModel.class;
  }

  @Nullable
  public String getValue(SectionInfo info) {
    List<String> vals = getModel(info).getValues();
    if (vals != null) {
      return vals.get(0);
    }
    return null;
  }

  public void setValue(SectionInfo info, @Nullable Object value) {
    HiddenStateModel model = getModel(info);
    if (value == null) {
      model.setValues(null);
    } else {
      model.setValues(Lists.newArrayList(value.toString()));
    }
  }

  public List<String> getValues(SectionInfo info) {
    List<String> vals = getModel(info).getValues();
    if (vals == null) {
      return new ArrayList<String>();
    }
    return vals;
  }

  public void setValues(SectionInfo info, Collection<String> vals) {
    getModel(info).setValues(new ArrayList<String>(vals));
  }

  @NonNullByDefault(false)
  public static class HiddenStateModel {
    private boolean beenRendered;
    private List<String> values;

    public List<String> getValues() {
      return values;
    }

    public void setValues(List<String> values) {
      this.values = values;
    }

    public boolean isBeenRendered() {
      return beenRendered;
    }

    public void setBeenRendered(boolean beenRendered) {
      this.beenRendered = beenRendered;
    }
  }

  @Nullable
  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    HiddenStateModel model = getModel(context);
    model.setBeenRendered(true);
    List<String> values = model.getValues();
    if (values != null) {
      HiddenInput hiddenFields = new HiddenInput();
      String paramId = getParameterId();
      for (String val : values) {
        hiddenFields.addField(null, paramId, val);
      }
      return hiddenFields;
    }
    return null;
  }

  @Override
  protected boolean hasBeenRendered(SectionInfo info) {
    return getModel(info).isBeenRendered();
  }

  @Override
  public void bookmark(SectionInfo info, BookmarkEvent event) {
    if (addToThisBookmark(info, event)) {
      HiddenStateModel model = getModel(info);
      List<String> values = model.getValues();
      if (values != null) {
        event.setParams(getParameterId(), values);
      }
    }
  }

  @Override
  public void document(SectionInfo info, DocumentParamsEvent event) {
    addDocumentedParam(event, getParameterId(), String.class.getName());
  }

  @Override
  public void handleParameters(SectionInfo info, ParametersEvent event) throws Exception {
    HiddenStateModel model = getModel(info);
    String[] vals = event.getParameterValues(getParameterId());
    if (vals != null) {
      model.setValues(Lists.newArrayList(vals));
    }
  }

  public int getIntValue(SectionInfo info, int defVal) {
    String val = getValue(info);
    if (!Check.isEmpty(val)) {
      return Integer.parseInt(val);
    }
    return defVal;
  }

  @Override
  public JSExpression createGetExpression() {
    return new FunctionCallExpression(FormValuesLibrary.GET_ALL_VALUES, getParameterId());
  }

  @Override
  public JSCallable createSetFunction() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JSCallable createResetFunction() {
    throw new UnsupportedOperationException();
  }
}
