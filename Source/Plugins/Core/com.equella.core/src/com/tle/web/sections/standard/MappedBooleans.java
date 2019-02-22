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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MappedBooleans extends AbstractHtmlComponent<MappedBooleans.MappedBooleansModel>
    implements ParametersEventListener, BookmarkEventListener {
  @Override
  public Object instantiateModel(SectionInfo info) {
    return new MappedBooleansModel();
  }

  public static class MappedBooleansModel {
    private final Set<String> checked = Sets.newHashSet();
    private final Map<String, HtmlBooleanState> renderered = Maps.newHashMap();

    public Set<String> getChecked() {
      return checked;
    }

    public Map<String, HtmlBooleanState> getRenderered() {
      return renderered;
    }
  }

  public HtmlBooleanState getBooleanState(SectionInfo info, String key) {
    MappedBooleansModel model = getModel(info);
    Map<String, HtmlBooleanState> rendered = model.getRenderered();
    HtmlBooleanState boolState = rendered.get(key);
    if (boolState == null) {
      boolState = new HtmlBooleanState();
      Set<String> checked = model.getChecked();
      boolState.setId(getParameterId() + '_' + SectionUtils.getPageUniqueId(info));
      boolState.setName(getParameterId());
      boolState.setValue(key);
      boolState.setChecked(checked.contains(key));
      rendered.put(key, boolState);
    }
    return boolState;
  }

  @Override
  public void handleParameters(SectionInfo info, ParametersEvent event) throws Exception {
    String[] parameters = event.getParameterValues(getParameterId());
    MappedBooleansModel model = getModel(info);
    if (parameters != null) {
      Collections.addAll(model.getChecked(), parameters);
    }
  }

  public void setCheckedSet(SectionInfo info, Collection<String> checked) {
    MappedBooleansModel model = getModel(info);
    Set<String> checkedSet = model.getChecked();
    checkedSet.clear();
    checkedSet.addAll(checked);
  }

  public String getFirstChecked(SectionInfo info) {
    MappedBooleansModel model = getModel(info);
    Set<String> checked = model.getChecked();
    if (checked.isEmpty()) {
      return null;
    }
    return checked.iterator().next();
  }

  public boolean isChecked(SectionInfo info, String id) {
    return getModel(info).getChecked().contains(id);
  }

  public Set<String> getCheckedSet(SectionInfo info) {
    MappedBooleansModel model = getModel(info);
    return Collections.unmodifiableSet(model.getChecked());
  }

  public void setValue(SectionInfo info, String key, boolean checked) {
    MappedBooleansModel model = getModel(info);
    Set<String> checkedSet = model.getChecked();
    if (!checked) {
      checkedSet.remove(key);
    } else {
      checkedSet.add(key);
    }
  }

  /**
   * This is specific to the renderer. See jolse.
   *
   * @return
   */
  @Deprecated
  public JSExpression getElementsExpression() {
    return Jq.$(Type.NAME, getParameterId());
  }

  public void clearChecked(SectionInfo info) {
    MappedBooleansModel model = getModel(info);
    model.getChecked().clear();
  }

  @Override
  public void bookmark(SectionInfo info, BookmarkEvent event) {
    if (addToThisBookmark(info, event)) {
      MappedBooleansModel model = getModel(info);
      Set<String> checked = model.getChecked();
      if (!checked.isEmpty()) {
        Set<String> checkedSet = checked;
        if (event.isRendering()) {
          checkedSet = new HashSet<String>(checked);
          checkedSet.removeAll(model.getRenderered().keySet());
        }
        event.setParams(getParameterId(), checkedSet);
      }
    }
  }

  @Override
  public void document(SectionInfo info, DocumentParamsEvent event) {
    addDocumentedParam(event, getParameterId() + "(*)", Boolean.TYPE.getName()); // $NON-NLS-1$
  }
}
