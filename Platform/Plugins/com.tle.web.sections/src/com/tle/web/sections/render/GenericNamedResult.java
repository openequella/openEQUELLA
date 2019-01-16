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

import com.tle.web.sections.NamedSectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

public class GenericNamedResult implements NamedSectionResult, StyleableRenderer {
  private final String name;
  private SectionRenderable renderer;

  public GenericNamedResult(String name, PreRenderable... renderers) {
    this.name = name;
    for (PreRenderable arender : renderers) {
      renderer = CombinedRenderer.combineResults(renderer, arender);
    }
  }

  public SectionRenderable getInner() {
    return renderer;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void preRender(PreRenderContext info) {
    SectionUtils.preRender(info, renderer);
  }

  @Override
  public void realRender(SectionWriter writer) throws IOException {
    if (renderer != null) {
      renderer.realRender(writer);
    }
  }

  @Override
  public void setStyles(String style, String styleClass, String id) {
    if (renderer instanceof StyleableRenderer) {
      ((StyleableRenderer) renderer).setStyles(style, styleClass, id);
    }
  }

  @Override
  public StyleableRenderer addClass(String extraClass) {
    if (renderer instanceof StyleableRenderer) {
      return ((StyleableRenderer) renderer).addClass(extraClass);
    }
    return null;
  }

  @Override
  public String toString() {
    return name + "=" + (renderer == null ? "(none)" : renderer.toString());
  }
}
