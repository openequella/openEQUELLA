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

package com.tle.web.sections.registry.handler;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.convert.Conversion;
import com.tle.web.sections.events.AbstractDirectEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.MethodInvocationEventGenerator;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("nls")
@NonNullByDefault
public class AnnotatedEventsScanner {
  public static class EventData {
    String name;
    Method eventMethod;
    EventHandlerMethod annotation;
    int numParams;
  }

  public static class DirectData {
    Method eventMethod;
    DirectEvent annotation;
  }

  private final Map<String, EventData> handlerMethods = new HashMap<String, EventData>();
  private final List<Field> factories = new ArrayList<Field>();
  private final List<DirectData> directEvents = new ArrayList<DirectData>();
  private final Conversion conversion;

  public AnnotatedEventsScanner(
      Class<?> clazz, EventFactoryHandler handler, Conversion conversion) {
    this.conversion = conversion;
    Method[] methods = clazz.getDeclaredMethods();
    for (Method method : methods) {
      EventHandlerMethod annotation = method.getAnnotation(EventHandlerMethod.class);
      if (annotation != null) {
        EventData handlerData = new EventData();

        Class<?>[] params = method.getParameterTypes();
        if (params.length == 0
            || (params[0] != SectionContext.class && params[0] != SectionInfo.class)) {
          throw new SectionsRuntimeException(
              "Event handler methods must start with SectionContext or SectionInfo parameter -"
                  + " we're all looking at you "
                  + clazz.getName()
                  + "."
                  + method.getName()
                  + "()");
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
      DirectEvent direct = method.getAnnotation(DirectEvent.class);
      if (direct != null) {
        DirectData directData = new DirectData();
        directData.annotation = direct;
        directData.eventMethod = method;
        directEvents.add(directData);
      }
    }

    for (Field field : clazz.getDeclaredFields()) {
      EventFactory annotation = field.getAnnotation(EventFactory.class);
      if (annotation != null) {
        field.setAccessible(true);
        factories.add(field);
      }
    }

    Class<?> parentClazz = clazz.getSuperclass();
    if (parentClazz != null) {
      AnnotatedEventsScanner scanner = handler.getForClass(parentClazz);
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
      factories.addAll(scanner.factories);
      directEvents.addAll(scanner.directEvents);
    }
  }

  @Nullable
  public EventGenerator registerEventFactories(Object section, String id, SectionTree tree) {
    for (DirectData data : directEvents) {
      tree.addApplicationEvent(
          new DirectMethodEvent(data.annotation.priority(), id, data.eventMethod));
    }

    if (factories.isEmpty()) {
      if (!handlerMethods.isEmpty()) {
        throw new SectionsRuntimeException(
            "No @EventFactory registered for " + section.getClass().getName());
      }
      return null;
    }

    final EventGenerator generator = new EventGenerator(id);
    for (Field factoryField : factories) {
      try {
        factoryField.set(section, generator);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    for (EventData data : handlerMethods.values()) {
      EventHandlerMethod annotation = data.annotation;
      MethodInvocationEventGenerator mgen =
          new MethodInvocationEventGenerator(
              id + '.' + data.name,
              id,
              section,
              data.eventMethod,
              annotation.priority(),
              annotation.preventXsrf(),
              conversion);
      generator.addEventCreator(data.name, mgen);
    }

    return generator;
  }

  public static class DirectMethodEvent extends AbstractDirectEvent {
    private final Method method;

    public DirectMethodEvent(int priority, String id, Method method) {
      super(priority, id);
      this.method = method;
    }

    @Override
    public void fireDirect(SectionId sectionId, SectionInfo info) throws Exception {
      try {
        SectionId section = info.getSectionForId(sectionId);
        if (method.getParameterTypes()[0] == SectionInfo.class) {
          method.invoke(section, info);
        } else {
          method.invoke(section, info.getContextForId(sectionId.getSectionId()));
        }
      } catch (InvocationTargetException e) {
        Throwable t = e.getTargetException();
        if (t instanceof Exception) {
          throw (Exception) t;
        } else if (t != null) {
          throw new RuntimeException(t);
        }
        throw e;
      }
    }
  }
}
