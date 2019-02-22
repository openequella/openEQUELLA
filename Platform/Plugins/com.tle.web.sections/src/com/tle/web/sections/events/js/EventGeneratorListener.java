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

import com.google.common.collect.Maps;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("nls")
public class EventGeneratorListener implements ParametersEventListener {
  public static final String NULL_VALUE = "$null$";

  public static final String EVENT_ID = "event__";
  public static final String EVENT_PARAM = "eventp__";

  private final Map<String, ParameterizedEvent> eventMap =
      new HashMap<String, ParameterizedEvent>();
  private static final String LISTENER_KEY = "$EVENT$LISTENER$";

  public static EventGeneratorListener getForTree(SectionTree tree) {
    EventGeneratorListener listener = tree.getAttribute(LISTENER_KEY);
    if (listener == null) {
      listener = new EventGeneratorListener();
      tree.addListener(null, ParametersEventListener.class, listener);
      tree.setAttribute(LISTENER_KEY, listener);
    }
    return listener;
  }

  public static Map<String, String[]> convertToParamMap(String... params) {
    Map<String, String[]> eventParams = Maps.newHashMap();
    for (int i = 0; i < params.length; i++) {
      String param = params[i];
      String[] paramArray = new String[] {param};
      if (i == 0) {
        eventParams.put(EVENT_ID, paramArray);
      } else {
        eventParams.put(EVENT_PARAM + (i - 1), paramArray);
      }
    }
    return eventParams;
  }

  @Override
  public void handleParameters(SectionInfo info, ParametersEvent event) throws Exception {
    String eventParam = event.getParameter(EVENT_ID, false);
    if (eventParam == null) {
      return;
    }

    ParameterizedEvent pevent = eventMap.get(eventParam);
    if (pevent != null) {
      event.parameterHandled(EVENT_ID);
      int numParams = pevent.getParameterCount();
      if (numParams < 0) {
        numParams = Integer.MAX_VALUE;
      }
      List<String> params = new ArrayList<String>();
      for (int i = 0; i < numParams; i++) {
        String[] paramVals = event.getParameterValues(EVENT_PARAM + i);
        if (paramVals == null) {
          break;
        }
        if (paramVals.length > 0) {
          final String value = paramVals[0];
          params.add(NULL_VALUE.equals(value) ? null : value);
        } else {
          params.add(null);
        }
      }
      info.queueEvent(pevent.createEvent(info, params.toArray(new String[params.size()])));
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends ParameterizedEvent> T getRegisteredHandler(String name) {
    return (T) eventMap.get(name);
  }

  public void registerHandler(ParameterizedEvent event) {
    String name = event.getEventId();
    if (eventMap.containsKey(name)) {
      throw new SectionsRuntimeException("Tree already contains event generator for name:" + name);
    }
    eventMap.put(name, event);
  }
}
