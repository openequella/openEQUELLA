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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMappedValues<T>
    extends AbstractHtmlComponent<AbstractMappedValues.MappedValuesModel<T>>
    implements ParametersEventListener, BookmarkEventListener {

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new MappedValuesModel<T>();
  }

  public static class MappedValuesModel<T> {
    private final Map<String, T> map = Maps.newHashMap();
    private final Set<String> rendered = Sets.newHashSet();

    public Map<String, T> getMap() {
      return map;
    }

    public Set<String> getRendered() {
      return rendered;
    }
  }

  public Map<String, T> getValuesMap(SectionInfo info) {
    MappedValuesModel<T> model = getModel(info);
    return Collections.unmodifiableMap(model.getMap());
  }

  public void setValuesMap(SectionInfo info, Map<String, T> map) {
    MappedValuesModel<T> model = getModel(info);
    Map<String, T> modelMap = model.getMap();
    modelMap.clear();
    modelMap.putAll(map);
  }

  // TODO: try/catch/BadRequestException
  @Override
  public void handleParameters(SectionInfo info, ParametersEvent event) throws Exception {
    MappedValuesModel<T> model = getModel(info);
    String prefix = getParameterId() + '(';
    Map<String, T> map = model.getMap();
    for (String paramName : event.getParameterNames()) {
      if (paramName.startsWith(prefix)) {
        String val = event.getParameterValues(paramName)[0];
        String key = keyUnEscape(paramName.substring(prefix.length(), paramName.length() - 1));
        map.put(key, convert(val));
      }
    }
  }

  protected abstract T convert(String val);

  protected abstract String convertBack(T val);

  public String keyEscape(String key) {
    StringBuilder sbuf = new StringBuilder();
    for (int i = 0; i < key.length(); i++) {
      char c = key.charAt(i);
      if (c == ')') {
        sbuf.append("\\1"); // $NON-NLS-1$
      } else if (c == '[') {
        sbuf.append("\\2"); // $NON-NLS-1$
      } else if (c == ']') {
        sbuf.append("\\3"); // $NON-NLS-1$
      } else if (c == '\\') {
        sbuf.append("\\\\"); // $NON-NLS-1$
      } else if (c == '(') {
        sbuf.append("\\4"); // $NON-NLS-1$
      } else {
        sbuf.append(c);
      }
    }
    return sbuf.toString();
  }

  public String keyUnEscape(String key) {
    StringBuilder sbuf = new StringBuilder();
    for (int i = 0; i < key.length(); i++) {
      char c = key.charAt(i);
      if (c == '\\') {
        c = key.charAt(++i);
        if (c == '1') {
          c = ')';
        } else if (c == '2') {
          c = '[';
        } else if (c == '3') {
          c = ']';
        } else if (c == '4') {
          c = '(';
        }
      }
      sbuf.append(c);
    }
    return sbuf.toString();
  }

  public void setValue(SectionInfo info, String key, T value) {
    MappedValuesModel<T> model = getModel(info);
    model.getMap().put(key, value);
  }

  @Override
  public void bookmark(SectionInfo info, BookmarkEvent event) {
    if (addToThisBookmark(info, event)) {
      MappedValuesModel<T> model = getModel(info);
      Set<String> rendered = model.getRendered();
      for (Map.Entry<String, T> entry : model.getMap().entrySet()) {
        String key = entry.getKey();
        if (!event.isRendering() || !rendered.contains(key)) {
          event.setParam(
              getParameterId() + '(' + keyEscape(key) + ')', convertBack(entry.getValue()));
        }
      }
    }
  }

  @SuppressWarnings("nls")
  @Override
  public void document(SectionInfo info, DocumentParamsEvent event) {
    addDocumentedParam(event, getParameterId() + "(*)", String.class.getName());
  }
}
