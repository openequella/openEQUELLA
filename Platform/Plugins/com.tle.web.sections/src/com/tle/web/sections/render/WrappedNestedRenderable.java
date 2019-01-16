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

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

public class WrappedNestedRenderable implements NestedRenderable {
  protected NestedRenderable nested;

  public WrappedNestedRenderable(NestedRenderable nested) {
    this.nested = nested;
  }

  @Override
  public SectionRenderable getNestedRenderable() {
    return nested.getNestedRenderable();
  }

  @Override
  public NestedRenderable setNestedRenderable(SectionRenderable nested) {
    this.nested.setNestedRenderable(nested);
    return this;
  }

  @Override
  public void realRender(SectionWriter writer) throws IOException {
    nested.realRender(writer);
  }

  @Override
  public void preRender(PreRenderContext info) {
    nested.preRender(info);
  }
}
