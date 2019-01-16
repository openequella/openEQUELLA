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

package com.tle.web.sections.standard.js;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSPropertyExpression;
import com.tle.web.sections.js.generic.expression.ArrayIndexExpression;
import com.tle.web.sections.js.generic.expression.CombinedPropertyExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;

public class JSONComponentMappings {
  private Map<String, JSValueComponent> mappings = new LinkedHashMap<String, JSValueComponent>();
  private Map<String, Map<String, JSValueComponent>> subMappings =
      new LinkedHashMap<String, Map<String, JSValueComponent>>();

  public void addMapping(String key, JSValueComponent component) {
    mappings.put(key, component);
  }

  public void addMapMapping(String map, String key, JSValueComponent component) {
    Map<String, JSValueComponent> subMap = subMappings.get(map);
    if (subMap == null) {
      subMap = new LinkedHashMap<String, JSValueComponent>();
      subMappings.put(map, subMap);
    }
    subMap.put(key, component);
  }

  public Map<JSPropertyExpression, JSExpression> createGetMappings() {
    Map<JSPropertyExpression, JSExpression> getMap =
        new LinkedHashMap<JSPropertyExpression, JSExpression>();

    for (Map.Entry<String, JSValueComponent> entry : mappings.entrySet()) {
      getMap.put(convertKey(entry.getKey()), entry.getValue().createGetExpression());
    }

    for (Map.Entry<String, Map<String, JSValueComponent>> entry : subMappings.entrySet()) {
      Map<String, JSValueComponent> subMap = entry.getValue();
      ObjectExpression obj = new ObjectExpression();
      for (Map.Entry<String, JSValueComponent> subentry : subMap.entrySet()) {
        obj.put(subentry.getKey(), subentry.getValue().createGetExpression());
      }
      getMap.put(convertKey(entry.getKey()), obj);
    }

    return getMap;
  }

  private JSPropertyExpression convertKey(String key) {
    return new ArrayIndexExpression(key);
  }

  public Set<String> getMapAttributeNames() {
    return subMappings.keySet();
  }

  public Map<JSPropertyExpression, JSCallable> createSetMappings() {
    Map<JSPropertyExpression, JSCallable> setMap =
        new LinkedHashMap<JSPropertyExpression, JSCallable>();
    for (String key : mappings.keySet()) {
      setMap.put(convertKey(key), mappings.get(key).createSetFunction());
    }
    for (String mapKey : subMappings.keySet()) {
      Map<String, JSValueComponent> subMap = subMappings.get(mapKey);
      JSPropertyExpression firstIndex = convertKey(mapKey);
      for (Map.Entry<String, JSValueComponent> entry : subMap.entrySet()) {
        setMap.put(
            new CombinedPropertyExpression(firstIndex, new ArrayIndexExpression(entry.getKey())),
            entry.getValue().createSetFunction());
      }
    }
    return setMap;
  }
}
