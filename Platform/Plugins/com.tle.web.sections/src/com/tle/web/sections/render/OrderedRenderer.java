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

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import java.io.IOException;
import java.util.Comparator;

public class OrderedRenderer implements SectionRenderable {
  private final int order;
  private final SectionRenderable renderer;

  public OrderedRenderer(int order, SectionRenderable renderer) {
    this.order = order;
    this.renderer = renderer;
  }

  public OrderedRenderer(int order, PreRenderable prerenderer) {
    this.order = order;
    if (prerenderer instanceof SectionRenderable) {
      this.renderer = (SectionRenderable) prerenderer;
    } else {
      this.renderer = new PreRenderOnly(prerenderer);
    }
  }

  @Override
  public void preRender(PreRenderContext info) {
    renderer.preRender(info);
  }

  @Override
  public void realRender(SectionWriter writer) throws IOException {
    renderer.realRender(writer);
  }

  public int getOrder() {
    return order;
  }

  public static class RendererOrder implements Comparator<OrderedRenderer> {
    @Override
    public int compare(OrderedRenderer o1, OrderedRenderer o2) {
      return o1.getOrder() - o2.getOrder();
    }
  }
}
