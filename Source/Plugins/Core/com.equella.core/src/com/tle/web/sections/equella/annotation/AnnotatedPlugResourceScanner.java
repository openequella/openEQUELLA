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

package com.tle.web.sections.equella.annotation;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.AbstractRenderedComponent;
import com.tle.web.sections.standard.model.SimpleBookmark;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class AnnotatedPlugResourceScanner {
  private final Class<?> clazz;
  private PluginResourceHelper helper;
  private final Map<Field, Label> componentLabels = new HashMap<Field, Label>();

  @SuppressWarnings("nls")
  public AnnotatedPlugResourceScanner(Class<?> clazz, PluginResourceHandler handler) {
    this.clazz = clazz;
    try {
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields) {
        PlugURL plugUrl = field.getAnnotation(PlugURL.class);
        final Class<?> type = field.getType();
        if (plugUrl != null) {
          checkStatic(clazz, field, "PlugURL");
          String url = getHelper().url(plugUrl.value());
          if (type == String.class) {
            field.set(null, url);
          } else if (type.isAssignableFrom(SimpleBookmark.class)) {
            field.set(null, new SimpleBookmark(url));
          } else if (type.isAssignableFrom(CssInclude.class)) {
            field.set(null, CssInclude.include(url).make());
          } else if (type.isAssignableFrom(IncludeFile.class)) {
            field.set(null, new IncludeFile(url));
          } else {
            throw new SectionsRuntimeException(
                "Unsupported type for @PlugURL must be Bookmark, CssInclude or String");
          }
        } else {
          PlugKey plugKey = field.getAnnotation(PlugKey.class);
          if (plugKey != null) {
            boolean global = plugKey.global();
            String key = global ? plugKey.value() : getHelper().key(plugKey.value());
            boolean html = plugKey.html();
            Icon icon = plugKey.icon();

            if (AbstractRenderedComponent.class.isAssignableFrom(type)) {
              field.setAccessible(true);
              Label label = new KeyLabel(html, key);
              if (icon != Icon.NONE) {
                label = new IconLabel(icon, label);
              }
              componentLabels.put(field, label);
            } else {
              checkStatic(clazz, field, "PlugKey");
              if (type == String.class) {
                field.set(null, key);
              } else if (type.isAssignableFrom(KeyLabel.class)) {
                Label label = new KeyLabel(html, key);
                if (icon != Icon.NONE) {
                  label = new IconLabel(icon, label);
                }
                field.set(null, label);
              } else if (type.isAssignableFrom(Confirm.class)) {
                field.set(null, new Confirm(new KeyLabel(html, key)));
              } else {
                throw new SectionsRuntimeException(
                    "Unsupported type for @PlugKey must be Label, Confirm, String or"
                        + " AbstractRenderedComponent");
              }
            }
          }
        }
      }
      clazz = clazz.getSuperclass();
      if (clazz != null) {
        AnnotatedPlugResourceScanner parentScanner = handler.getForClass(clazz);
        if (parentScanner != null) {
          componentLabels.putAll(parentScanner.componentLabels);
        }
      }
    }
    // In the interests of diagnostics, we'll allow an explicit catch of
    // generic exception
    catch (Exception e) // NOSONAR
    {
      SectionUtils.throwRuntime(e);
    }
  }

  @SuppressWarnings("nls")
  public void setupLabels(Section section) {
    for (Field field : componentLabels.keySet()) {
      try {
        AbstractRenderedComponent<?> comp = (AbstractRenderedComponent<?>) field.get(section);
        comp.setLabel(componentLabels.get(field));
      } catch (Exception e) {
        throw new RuntimeException(
            field.getName()
                + " on "
                + field.getDeclaringClass()
                + " has @PlugKey but probably missing @Component?",
            e);
      }
    }
  }

  @SuppressWarnings("nls")
  private void checkStatic(Class<?> clazz, Field field, String annotation) {
    field.setAccessible(true);
    if ((field.getModifiers() & Modifier.STATIC) == 0) {
      throw new SectionsRuntimeException(
          "Field '"
              + field
              + "' of class '"
              + clazz
              + "' must be declared static to use the @"
              + annotation
              + " annotation");
    }
  }

  private PluginResourceHelper getHelper() {
    if (helper == null) {
      helper = ResourcesService.getResourceHelper(clazz);
    }
    return helper;
  }
}
