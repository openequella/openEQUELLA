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

package com.tle.web.sections.ajax.handler;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.convert.Conversion;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AnnotatedAjaxMethodScanner {
  public static class EventData {
    String name;
    Method eventMethod;
    AjaxMethod annotation;
    int numParams;
  }

  public static class FactoryData {
    String name;
    Field factoryField;
  }

  private final Map<String, EventData> handlerMethods = new HashMap<String, EventData>();
  private final Map<String, FactoryData> factories = new HashMap<String, FactoryData>();
  private Conversion conversion;

  public AnnotatedAjaxMethodScanner(
      Class<?> clazz, AjaxMethodHandler ajaxScanner, Conversion conversion) {
    this.conversion = conversion;
    Method[] methods = clazz.getDeclaredMethods();
    for (Method method : methods) {
      AjaxMethod annotation = method.getAnnotation(AjaxMethod.class);
      if (annotation != null) {
        EventData handlerData = new EventData();

        Class<?>[] params = method.getParameterTypes();
        Class<?> firstParam = params[0];
        if (firstParam != SectionInfo.class && firstParam != AjaxRenderContext.class) {
          throw new SectionsRuntimeException(
              "Ajax Event handler methods must start with SectionInfo or AjaxRenderContext"
                  + " parameter"); //$NON-NLS-1$
        }
        handlerData.numParams = params.length - 1;
        handlerData.eventMethod = method;
        handlerData.annotation = annotation;
        String name = annotation.name();
        if (name.isEmpty()) {
          name = method.getName();
        }
        handlerData.name = name;
        handlerMethods.put(handlerData.name, handlerData);
      }
    }
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      AjaxFactory annotation = field.getAnnotation(AjaxFactory.class);
      if (annotation != null) {
        String name = annotation.name();
        FactoryData factoryData = new FactoryData();
        factoryData.factoryField = field;
        factoryData.name = name;
        field.setAccessible(true);
        factories.put(name, factoryData);
      }
    }
    Class<?> parentClazz = clazz.getSuperclass();
    if (parentClazz != null) {
      AnnotatedAjaxMethodScanner scanner = ajaxScanner.getForClass(parentClazz);
      // check for overridden handler methods
      for (Map.Entry<String, EventData> entry : scanner.handlerMethods.entrySet()) {
        EventData data = entry.getValue();
        Method eventMethod = data.eventMethod;
        try {
          Method method = clazz.getMethod(eventMethod.getName(), eventMethod.getParameterTypes());
          EventData newdata = new EventData();
          newdata.annotation = data.annotation;
          newdata.eventMethod = method;
          newdata.name = data.name;
          newdata.numParams = data.numParams;
          data = newdata;
        } catch (NoSuchMethodException nsme) {
          // nout
        }
        handlerMethods.put(entry.getKey(), data);
      }
      factories.putAll(scanner.factories);
    }
  }

  public Collection<AjaxGeneratorImpl> registerAjaxFactories(
      Object section, String id, SectionTree tree) {
    Map<String, AjaxGeneratorImpl> generators = new HashMap<String, AjaxGeneratorImpl>();
    for (FactoryData factoryData : factories.values()) {
      AjaxGeneratorImpl generator = new AjaxGeneratorImpl(id);
      generators.put(factoryData.name, generator);
      try {
        factoryData.factoryField.set(section, generator);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    for (EventData data : handlerMethods.values()) {
      AjaxMethod annotation = data.annotation;
      AjaxEventCreator mgen =
          new AjaxEventCreator(
              id + '.' + data.name, id, data.eventMethod, annotation.priority(), conversion);
      AjaxGeneratorImpl generator = generators.get(annotation.factoryName());
      if (generator == null) {
        throw new SectionsRuntimeException(
            "No @AjaxFactory's registered for class: " + section.getClass()); // $NON-NLS-1$
      }
      generator.addEventCreator(data.name, mgen);
    }
    return generators.values();
  }
}
