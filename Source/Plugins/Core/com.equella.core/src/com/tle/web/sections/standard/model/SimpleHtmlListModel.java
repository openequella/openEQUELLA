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

package com.tle.web.sections.standard.model;

import com.tle.common.NameValue;
import com.tle.web.sections.SectionInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleHtmlListModel<T> implements HtmlListModel<T> {
  private final Map<String, Option<T>> valMap = new LinkedHashMap<String, Option<T>>();
  private final List<Option<T>> options = new ArrayList<Option<T>>();

  public SimpleHtmlListModel() {
    // nothing
  }

  public SimpleHtmlListModel(Object... values) {
    addAll(Arrays.asList(values));
  }

  public SimpleHtmlListModel(Collection<?> values) {
    addAll(values);
  }

  @SuppressWarnings("unchecked")
  public void add(Object obj) {
    Option<T> opt;
    if (obj instanceof Option) {
      opt = (Option<T>) obj;
    } else {
      opt = convertToOption((T) obj);
    }

    options.add(opt);
    valMap.put(opt.getValue(), opt);
  }

  protected Option<T> convertToOption(T obj) {
    return defaultConvertToOption(obj);
  }

  public void addAll(Collection<?> objs) {
    for (Object obj : objs) {
      add(obj);
    }
  }

  @Override
  public Option<T> getOption(SectionInfo info, String value) {
    return valMap.get(value);
  }

  @Override
  public List<Option<T>> getOptions(SectionInfo info) {
    return options;
  }

  public static <T> Option<T> defaultConvertToOption(T obj) {
    if (obj instanceof Option) {
      throw new Error("This should not happen anymore"); // $NON-NLS-1$
    }
    if (obj instanceof NameValue) {
      return new NameValueOption<T>((NameValue) obj, obj);
    }
    String strVal = obj.toString();
    return new SimpleOption<T>(strVal, strVal, obj);
  }

  @Override
  public List<T> getValues(SectionInfo info, Collection<String> values) {
    List<T> vals = new ArrayList<T>();
    for (String value : values) {
      T val = getValue(info, value);
      if (val != null) {
        vals.add(val);
      }
    }
    return vals;
  }

  @Override
  public T getValue(SectionInfo info, String value) {
    Option<T> option = getOption(info, value);
    if (option != null) {
      return option.getObject();
    }
    return null;
  }

  @Override
  public String getDefaultValue(SectionInfo info) {
    final List<Option<T>> opts = getOptions(info);
    if (!opts.isEmpty()) {
      return opts.get(0).getValue();
    }
    return null;
  }

  @Override
  public Set<String> getMatchingValues(SectionInfo info, Set<String> values) {
    Set<String> keySet = valMap.keySet();
    if (keySet.containsAll(values)) {
      return values;
    }
    Set<String> matching = new HashSet<String>();
    for (String val : values) {
      if (keySet.contains(val)) {
        matching.add(val);
      }
    }
    return matching;
  }

  @Override
  public String getStringValue(SectionInfo info, T value) {
    return convertToOption(value).getValue();
  }
}
