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

package com.tle.web.sections.registry.handler;

import com.tle.common.util.CachedPropertyInfo;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.annotations.AfterTreeLookup;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.registry.handler.util.FieldAccessor;
import com.tle.web.sections.registry.handler.util.MethodAccessor;
import com.tle.web.sections.registry.handler.util.PropertyAccessor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AnnotatedTreeLookupScanner {
  public static class LookupData {
    Object key;
    PropertyAccessor field;
    TreeLookup annotation;
  }

  private final List<LookupData> lookups = new ArrayList<LookupData>();

  public AnnotatedTreeLookupScanner(Class<?> clazz, TreeLookupRegistrationHandler handler) {
    CachedPropertyInfo cachedBeanInfo = CachedPropertyInfo.forClass(clazz);
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      TreeLookup annotation = field.getAnnotation(TreeLookup.class);
      if (annotation != null) {
        Object key;
        String keyName = annotation.key();
        if (!keyName.isEmpty()) {
          key = keyName;
        } else {
          Class<?> type = field.getType();
          key = type;
        }
        LookupData data = new LookupData();
        data.key = key;
        PropertyDescriptor descriptor = cachedBeanInfo.getPropertyDescriptor(field.getName());
        if (descriptor == null || descriptor.getWriteMethod() == null) {
          data.field = new FieldAccessor(field);
        } else {
          data.field = new MethodAccessor(descriptor);
        }
        data.annotation = annotation;
        field.setAccessible(true);
        lookups.add(data);
      }
    }
    Method[] methods = clazz.getDeclaredMethods();
    for (Method method : methods) {
      TreeLookup annotation = method.getAnnotation(TreeLookup.class);
      if (annotation != null) {
        Object key;
        String keyName = annotation.key();
        if (!keyName.isEmpty()) {
          key = keyName;
        } else {
          Class<?> type = method.getParameterTypes()[0];
          key = type;
        }
        LookupData data = new LookupData();
        data.key = key;
        data.field = new MethodAccessor(null, method, method.getName());
        data.annotation = annotation;
        method.setAccessible(true);
        lookups.add(data);
      }
    }
    clazz = clazz.getSuperclass();
    if (clazz != null) {
      AnnotatedTreeLookupScanner scanner = handler.getForClass(clazz);
      lookups.addAll(scanner.lookups);
    }
  }

  public boolean hasLookups() {
    return !lookups.isEmpty();
  }

  public void doLookup(SectionTree tree, Section section) {
    for (LookupData data : lookups) {
      try {
        Object lookedUp = null;
        if (data.key instanceof Class) {
          if (SectionId.class.isAssignableFrom((Class<?>) data.key)) {
            Class<? extends SectionId> classKey = (Class<? extends SectionId>) data.key;
            lookedUp = tree.lookupSection(classKey, section);
          }
        }

        if (lookedUp == null) {
          // Seriously, this should never happen
          lookedUp = tree.getAttribute(data.key);

          if (lookedUp == null && data.annotation.mandatory()) {
            throw new SectionsRuntimeException(
                "Lookup of property: "
                    + data.field.getName()
                    + " of class: "
                    + section.getClass().getName()
                    + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          }
        }
        if (lookedUp != null) {
          data.field.write(section, lookedUp);
        }
      } catch (Exception e) {
        SectionUtils.throwRuntime(e);
      }
    }
    if (section instanceof AfterTreeLookup) {
      ((AfterTreeLookup) section).afterTreeLookup(tree);
    }
  }
}
