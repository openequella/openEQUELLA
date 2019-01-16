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

package com.tle.web.sections.render;

import java.io.IOException;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;

public class SectionIdRenderer implements SectionRenderable {
  private final SectionId id;

  public SectionIdRenderer(SectionId id) {
    this.id = id;
  }

  public SectionRenderable getRealRenderer(RenderContext context) {
    SectionRenderable renderable = context.getAttribute(this);
    if (renderable == null) {
      renderable = SectionUtils.renderSection(context, id);
      context.setAttribute(this, renderable);
    }
    return renderable;
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(getRealRenderer(info));
  }

  @Override
  public void realRender(SectionWriter writer) throws IOException {
    SectionRenderable realRenderer = getRealRenderer(writer);
    if (realRenderer != null) {
      realRenderer.realRender(writer);
    }
  }
}
