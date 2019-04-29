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

package com.tle.common.util;

import com.google.common.collect.Maps;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class CachedPropertyInfo {
  private static CacheHolder cache = new CacheHolder();

  private Map<String, PropertyDescriptor> propertyMap = Maps.newHashMap();

  private CachedPropertyInfo(Class<?> clazz) throws IntrospectionException {
    Class<?> thisClass = clazz;
    while (thisClass != null) {
      for (Field field : thisClass.getDeclaredFields()) {
        final Class<?> type = field.getType();
        final String name = field.getName();
        final String capsField = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        final Method readMethod =
            getMethodOrNot(
                thisClass,
                (type == Boolean.class || type == boolean.class ? "is" : "get") + capsField);
        final Method writeMethod = getMethodOrNot(thisClass, "set" + capsField, type);

        final PropertyDescriptor pd = new PropertyDescriptor(name, readMethod, writeMethod);
        propertyMap.put(pd.getName(), pd);
      }
      thisClass = thisClass.getSuperclass();
    }
  }

  private Method getMethodOrNot(Class<?> clazz, String methodName, Class<?>... params) {
    try {
      return clazz.getMethod(methodName, params);
    } catch (Exception e) {
      return null;
    }
  }

  public PropertyDescriptor getPropertyDescriptor(String name) {
    return propertyMap.get(name);
  }

  public PropertyDescriptor findPropertyForMethod(Method method) {
    for (PropertyDescriptor pd : propertyMap.values()) {
      if (pd.getReadMethod() == method || pd.getWriteMethod() == method) {
        return pd;
      }
    }
    return null;
  }

  public static CachedPropertyInfo forClass(Class<?> clazz) {
    return cache.getForClass(clazz);
  }

  private static class CacheHolder extends CachedClassData<CachedPropertyInfo> {
    @Override
    protected CachedPropertyInfo newEntry(Class<?> clazz) {
      try {
        return new CachedPropertyInfo(clazz);
      } catch (IntrospectionException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
