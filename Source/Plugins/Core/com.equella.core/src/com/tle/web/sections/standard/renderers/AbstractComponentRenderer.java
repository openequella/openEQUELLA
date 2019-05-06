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

package com.tle.web.sections.standard.renderers;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.model.HtmlComponentState;
import java.io.IOException;
import java.util.Map;

public abstract class AbstractComponentRenderer extends TagRenderer {
  protected HtmlComponentState state;

  public AbstractComponentRenderer(HtmlComponentState state) {
    super(null, state);
    this.state = state;
  }

  @SuppressWarnings("nls")
  @Override
  protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs)
      throws IOException {
    if (isDisabled()) {
      attrs.put("disabled", "disabled");
    }
  }

  public boolean isDisabled() {
    return state.isDisabled();
  }

  @Override
  public void realRender(SectionWriter writer) throws IOException {
    state.setBeenRendered(true);
    super.realRender(writer);
  }

  protected boolean isStillAddClickHandler() {
    return true;
  }

  @Override
  protected void processHandler(
      SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler) {
    if (!isStillAddClickHandler() && isDisabled() && JSHandler.EVENT_CLICK.equals(event)) {
      return;
    }
    super.processHandler(writer, attrs, event, handler);
  }

  @Override
  public SectionRenderable getNestedRenderable() {
    if (nestedRenderable != null) {
      return nestedRenderable;
    } else if (state.getLabel() != null) {
      nestedRenderable = state.createLabelRenderer();
    }
    return nestedRenderable;
  }

  public String getLabelText() {
    return state.getLabelText();
  }

  @Override
  protected abstract String getTag();

  public HtmlComponentState getHtmlState() {
    return state;
  }
}
