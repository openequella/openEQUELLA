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

package com.tle.web.sections.standard.impl;

import com.google.common.base.Preconditions;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.model.HtmlComponentState;
import java.io.IOException;

public class RenderFactoryRenderer implements SectionRenderable {
  private final RendererFactory renderFactory;
  private final HtmlComponentState state;

  private SectionRenderable realRenderer;

  public RenderFactoryRenderer(HtmlComponentState state, RendererFactory renderFactory) {
    this.state = Preconditions.checkNotNull(state);
    this.renderFactory = Preconditions.checkNotNull(renderFactory);
  }

  @Override
  public void realRender(SectionWriter writer) throws IOException {
    getRealRenderer(writer).realRender(writer);
  }

  private SectionRenderable getRealRenderer(RenderContext info) {
    if (realRenderer == null) {
      realRenderer = renderFactory.getRenderer(info, state);
      state.fireRendererCallback(info, realRenderer);
    }
    return realRenderer;
  }

  @Override
  public void preRender(PreRenderContext info) {
    getRealRenderer(info).preRender(info);
  }
}
