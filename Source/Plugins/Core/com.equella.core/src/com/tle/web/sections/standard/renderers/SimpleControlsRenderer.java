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

package com.tle.web.sections.standard.renderers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.dialog.model.ControlsState;
import com.tle.web.sections.standard.dialog.model.DialogControl;

public class SimpleControlsRenderer implements SectionRenderable {
  private ControlsState state;

  public SimpleControlsRenderer(ControlsState state) {
    this.state = state;
  }

  @Override
  public void realRender(SectionWriter writer) throws IOException {
    List<SectionRenderable> rendered = new ArrayList<SectionRenderable>();
    List<DialogControl> controls = state.getControls();
    for (DialogControl control : controls) {
      rendered.add(SectionUtils.renderSection(writer, control.getControl()));
    }
    for (SectionRenderable renderable : rendered) {
      writer.render(renderable);
    }
  }

  @Override
  public void preRender(PreRenderContext info) {
    // Nothing to do here
  }
}
