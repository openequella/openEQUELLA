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

package com.tle.core.xstream;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/** */
public class ReflectionProvider {
  private static final Class<?>[] BLANK_CLASSES = new Class[0];
  private static final Object[] BLANK_OBJECTS = new Object[0];

  public ReflectionProvider() {
    super();
  }

  public Object newInstance(Class<?> type) {
    Object instance = null;
    try {
      Constructor<?> constructor = type.getDeclaredConstructor(BLANK_CLASSES);
      constructor.setAccessible(true);
      instance = constructor.newInstance(BLANK_OBJECTS);
    } catch (Exception e) {
      throw new Error("Unable to find constructor for class: " + type, e); // $NON-NLS-1$
    }
    return instance;
  }

  public void writeField(Object object, String fieldName, Object value) {
    if (object == null) {
      return;
    }

    Class<?> definedIn = object.getClass();
    Class<?> type = getField(definedIn, fieldName).getType();
    boolean write = true;
    try {
      if (type == Integer.TYPE) {
        value = Integer.valueOf(value.toString());
      } else if (type == Boolean.TYPE) {
        value = Boolean.valueOf(value.toString());
      } else if (type == Long.TYPE) {
        value = Long.valueOf(value.toString());
      } else if (type == Float.TYPE) {
        value = Float.valueOf(value.toString());
      } else if (type == Short.TYPE) {
        value = Short.valueOf(value.toString());
      } else if (type == Double.TYPE) {
        value = Double.valueOf(value.toString());
      }

    } catch (Exception e) {
      write = false;
    }

    if (write) {
      try {
        getField(definedIn, fieldName).set(object, value);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public Object getField(Object object, String field) {
    Object value = null;
    if (object != null) {
      Field ffield = getField(object.getClass(), field);
      if (ffield != null) {
        try {
          value = ffield.get(object);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return value;
  }

  protected Field getField(Class<?> c, String name) {
    Field field = null;

    while (field == null && c != null) {
      try {
        field = c.getDeclaredField(name);
        field.setAccessible(true);
      } catch (Exception ex) {
        c = c.getSuperclass();
      }
    }
    if (field == null) {
      throw new RuntimeException(name + " doesn't exist in class " + c);
    }
    return field;
  }
}
