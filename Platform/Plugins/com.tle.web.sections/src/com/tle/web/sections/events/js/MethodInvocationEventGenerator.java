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

package com.tle.web.sections.events.js;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.convert.Conversion;
import com.tle.web.sections.events.AbstractDirectEvent;
import com.tle.web.sections.events.EventAuthoriser;
import com.tle.web.sections.events.SectionEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

@NonNullByDefault
public class MethodInvocationEventGenerator implements ParameterizedEvent {
  private final Method method;
  private final Object instance;
  private final String forId;
  private final int numParams;
  private final int priority;
  private final boolean preventXsrf;
  private final boolean useContext;
  private final String eventId;
  private final Conversion conversion;

  public MethodInvocationEventGenerator(
      String eventId,
      String id,
      Object instance,
      Method method,
      int priority,
      boolean preventXsrf,
      Conversion conversion) {
    this.conversion = conversion;
    this.eventId = eventId;
    this.forId = id;
    this.instance = instance;
    this.method = method;
    this.priority = priority;
    this.preventXsrf = preventXsrf;
    this.useContext = method.getParameterTypes()[0] == SectionContext.class;

    numParams = method.getParameterTypes().length - 1;
  }

  @Override
  public String getEventId() {
    return eventId;
  }

  @Override
  public SectionEvent<?> createEvent(SectionInfo info, final String[] params) {
    return new AbstractDirectEvent(priority, forId) {
      @Override
      public void fireDirect(SectionId sectionId, SectionInfo info) {
        if (preventXsrf) {
          EventAuthoriser authoriser = info.getAttributeForClass(EventAuthoriser.class);
          if (authoriser != null) {
            authoriser.checkAuthorisation(info);
          }
        }
        Object[] args = new Object[params.length + 1];
        if (useContext) {
          args[0] = info.getContextForId(sectionId.getSectionId());
        } else {
          args[0] = info;
        }
        Type[] paramTypes = method.getGenericParameterTypes();
        int i = 1;
        for (String param : params) {
          args[i] = conversion.convertFromString(param, paramTypes[i]);
          i++;
        }
        try {
          method.invoke(instance, args);
        } catch (Exception e) {
          SectionUtils.throwRuntime(e);
        }
      }
    };
  }

  @Override
  public int getParameterCount() {
    return numParams;
  }

  @Override
  public boolean isPreventXsrf() {
    return preventXsrf;
  }
}
