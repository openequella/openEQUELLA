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

package com.tle.web.sections.standard.renderers.popup;

import java.util.Map;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;

public class PopupLinkRenderer extends LinkRenderer {
  private PopupHelper helper = new PopupHelper();

  public void setHeight(String height) {
    helper.setHeight(height);
  }

  @Override
  public void setTarget(String target) {
    helper.setTarget(target);
  }

  public void setWidth(String width) {
    helper.setWidth(width);
  }

  public PopupLinkRenderer(HtmlLinkState state) {
    super(state);
  }

  public PopupLinkRenderer(HtmlComponentState state) {
    super(state);
  }

  @Override
  protected String getHref(SectionInfo info) {
    if (!helper.hasClientBookmark(info, state) && helper.hasServerBookmark(info, state)) {
      return helper.getHrefForClickEvent(info, state.getHandler(JSHandler.EVENT_CLICK));
    }
    return super.getHref(info);
  }

  @Override
  protected void processHandler(
      SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler) {
    if (!event.equals(JSHandler.EVENT_CLICK)) {
      super.processHandler(writer, attrs, event, handler);
    }
  }

  @Override
  public void ensureClickable() {
    // always clickable;
  }

  @Override
  protected void prepareLastAttributes(SectionWriter writer, Map<String, String> attrs) {
    if (!isDisabled()) {
      writer.bindHandler(
          JSHandler.EVENT_CLICK,
          attrs,
          helper.createClickHandler(
              writer, super.getHref(writer), state.getHandler(JSHandler.EVENT_CLICK)));
    }
  }
}
