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

package com.tle.web.sections.ajax.handler;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.JSONResponseCallback;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGeneratorListener;
import com.tle.web.sections.events.js.ParameterizedEvent;

public class UpdateDomEvent implements ParameterizedEvent {
  private final String eventId;
  private final UpdateDomKey key;

  public UpdateDomEvent(String eventId, UpdateDomKey key) {
    this.key = key;
    this.eventId = eventId;
  }

  @Override
  public String getEventId() {
    return eventId;
  }

  @Override
  public SectionEvent<?> createEvent(SectionInfo info, String[] params) {
    Set<String> ajaxIds = key.getAjaxIds();
    SectionId modalId = key.getModalId();
    AjaxRenderContext renderContext = new StandardAjaxRenderContext(info.getRootRenderContext());
    info.preventGET();

    renderContext.addAjaxDivs(ajaxIds);
    renderContext.setFormBookmarkEvent(
        new BookmarkEvent(modalId, true, modalId != null ? info : null));
    if (modalId != null) {
      renderContext.setModalId(modalId.getSectionId());
    }
    renderContext.setJSONResponseCallback(
        new JSONResponseCallback() {
          @Override
          public Object getResponseObject(AjaxRenderContext context) {
            return context.getFullDOMResult();
          }
        });

    ParameterizedEvent innerEvent = key.getInnerEvent();
    if (innerEvent != null) {
      return innerEvent.createEvent(info, params);
    }
    return null;
  }

  @Override
  public int getParameterCount() {
    if (key.getInnerEvent() != null) {
      return key.getInnerEvent().getParameterCount();
    }
    return 0;
  }

  @SuppressWarnings("nls")
  public static UpdateDomEvent register(
      SectionTree tree, SectionId modalId, ParameterizedEvent wrapped, String... ajaxIds) {
    UpdateDomKey key = new UpdateDomKey(modalId, ImmutableSet.copyOf(ajaxIds), wrapped);
    Map<UpdateDomKey, UpdateDomEvent> updateEvents = tree.getAttribute(UpdateDomEvent.class);
    if (updateEvents == null) {
      updateEvents = Maps.newHashMap();
      tree.setAttribute(UpdateDomEvent.class, updateEvents);
    }
    UpdateDomEvent already = updateEvents.get(key);
    if (already != null) {
      return already;
    }
    String eventId = tree.getRootId() + "$UP" + updateEvents.size() + '$';
    if (wrapped != null) {
      eventId += wrapped.getEventId();
    }
    UpdateDomEvent event = new UpdateDomEvent(eventId, key);
    EventGeneratorListener listener = EventGeneratorListener.getForTree(tree);
    listener.registerHandler(event);
    updateEvents.put(key, event);
    return event;
  }

  @Override
  public boolean isPreventXsrf() {
    return false;
  }
}
