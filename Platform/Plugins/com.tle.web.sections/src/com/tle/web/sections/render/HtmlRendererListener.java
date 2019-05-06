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

package com.tle.web.sections.render;

import com.tle.web.sections.Section;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.RenderEventListener;
import com.tle.web.sections.registry.handler.TargetedListener;

public class HtmlRendererListener extends TargetedListener implements RenderEventListener {

  private HtmlRenderer renderer;

  public HtmlRendererListener(String id, Section section, SectionTree tree) {
    super(id, section, tree);
    this.renderer = (HtmlRenderer) section;
  }

  @Override
  public void render(RenderEventContext context) {
    try {
      if (renderer instanceof ModalRenderer) {
        if (((ModalRenderer) renderer).isModal(context)) {
          return;
        }
      }
      SectionResult result = renderer.renderHtml(context);
      if (result != null) {
        context.getRenderEvent().returnResult(result);
      }
    } catch (Exception e) {
      SectionUtils.throwRuntime(e);
    }
  }
}
