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

package com.tle.web.sections.events.js;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.js.JSUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@NonNullByDefault
public class EventGenerator {
  private final Map<String, ParameterizedEvent> eventMap =
      new HashMap<String, ParameterizedEvent>();
  private final String forId;

  public EventGenerator(String forId) {
    this.forId = forId;
  }

  public void addEventCreator(String methodName, ParameterizedEvent pevent) {
    eventMap.put(methodName, pevent);
  }

  public SubmitValuesHandler getNamedHandler(String name, Object... params) {
    return getSubmitValuesHandler(name, params);
  }

  public SubmitValuesHandler getSubmitValuesHandler(String name, Object... params) {
    ParameterizedEvent pevent = getEventHandler(name);
    if (pevent.getParameterCount() != params.length) {
      throw new SectionsRuntimeException(
          "Wrong number of parameters to handler named '" + name + "'"); // $NON-NLS-1$//$NON-NLS-2$
    }
    return new SubmitValuesHandler(
        new SubmitValuesFunction(pevent), JSUtils.convertExpressions(params));
  }

  public ParameterizedEvent getEventHandler(String name) {
    ParameterizedEvent pevent = eventMap.get(name);
    if (pevent == null) {
      throw new SectionsRuntimeException("No handler method called:" + name); // $NON-NLS-1$
    }
    return pevent;
  }

  public JSBookmarkModifier getNamedModifier(String name, Object... params) {
    return new EventModifier(forId + '.' + name, getEventHandler(name), params);
  }

  public SubmitValuesFunction getSubmitValuesFunction(String name) {
    return new SubmitValuesFunction(getEventHandler(name));
  }

  public Collection<ParameterizedEvent> getEventsToRegister() {
    return eventMap.values();
  }

  public String getSectionId() {
    return forId;
  }
}
