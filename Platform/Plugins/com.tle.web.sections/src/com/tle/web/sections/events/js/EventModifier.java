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

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.EventAuthoriser;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.ServerSideValue;
import java.util.HashMap;
import java.util.Map;

public class EventModifier implements JSBookmarkModifier {
  private final JSExpression[] parameters;
  private final String eventId;
  private final ParameterizedEvent event;

  public EventModifier(String eventId, ParameterizedEvent event, Object... params) {
    this.eventId = eventId;
    this.event = event;
    this.parameters = JSUtils.convertExpressions(params);
  }

  @Override
  public Map<String, JSExpression> getClientExpressions() {
    Map<String, JSExpression> clientSide = new HashMap<String, JSExpression>();

    int i = 0;
    for (JSExpression expr : parameters) {
      if (!(expr instanceof ServerSideValue)) {
        clientSide.put(EventGeneratorListener.EVENT_PARAM + i, expr);
      }
      i++;
    }
    return clientSide;
  }

  @Override
  public boolean hasClientModifications() {
    for (JSExpression expr : parameters) {
      if (!(expr instanceof ServerSideValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void addToBookmark(SectionInfo info, Map<String, String[]> bookmarkState) {
    EventAuthoriser eventAuthoriser = info.getAttributeForClass(EventAuthoriser.class);
    if (eventAuthoriser != null && event.isPreventXsrf()) {
      eventAuthoriser.addToBookmark(info, bookmarkState);
    }
    bookmarkState.put(EventGeneratorListener.EVENT_ID, new String[] {eventId});
    int i = 0;
    for (JSExpression expr : parameters) {
      if (expr instanceof ServerSideValue) {
        bookmarkState.put(
            EventGeneratorListener.EVENT_PARAM + i,
            new String[] {((ServerSideValue) expr).getJavaString()});
      }
      i++;
    }
  }

  @Override
  public String getEventId() {
    return eventId;
  }

  @Override
  public JSExpression[] getParameters() {
    return parameters;
  }
}
