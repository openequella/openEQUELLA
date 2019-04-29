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
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlValueState;
import java.util.Objects;

public class MappedStrings extends AbstractMappedValues<String> {
  public HtmlBooleanState getBooleanState(SectionInfo info, String key, String value) {
    HtmlBooleanState state = new HtmlBooleanState();
    String name = getIdForKey(info, key);
    state.setName(name);
    state.setId(getParameterId() + '_' + SectionUtils.getPageUniqueId(info));
    MappedValuesModel<String> model = getModel(info);
    String text = model.getMap().get(key);
    if (Objects.equals(text, value)) {
      state.setChecked(true);
    }
    state.setValue(value);
    model.getRendered().add(key);
    return state;
  }

  public HtmlValueState getValueState(SectionInfo info, String key) {
    HtmlValueState value = new HtmlValueState();
    String nameId = getIdForKey(info, key);
    value.setId(getParameterId() + '_' + SectionUtils.getPageUniqueId(info));
    value.setName(nameId);
    MappedValuesModel<String> model = getModel(info);
    String text = model.getMap().get(key);
    model.getRendered().add(key);
    value.setValue(text != null ? text : ""); // $NON-NLS-1$
    return value;
  }

  public String getIdForKey(SectionInfo info, String key) {
    return getParameterId() + '(' + keyEscape(key) + ')';
  }

  @Override
  protected String convert(String val) {
    return val;
  }

  @Override
  protected String convertBack(String val) {
    return val;
  }
}
