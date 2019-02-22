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

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import java.io.IOException;

public class LabelRenderer implements SectionRenderable {
  protected final Label label;

  public Label getLabel() {
    return label;
  }

  public LabelRenderer(Label label) {
    this.label = label;
    if (label == null) {
      throw new NullPointerException();
    }
  }

  @Override
  public void realRender(SectionWriter writer) throws IOException {
    String text = label.getText();
    if (text != null) {
      if (label.isHtml()) {
        writer.write(text);
      } else {
        writer.writeText(text);
      }
    }
  }

  @Override
  public String toString() {
    if (label.isHtml()) {
      return label.getText();
    }
    return SectionUtils.ent(label.getText());
  }

  @Override
  public void preRender(PreRenderContext info) {
    // nothing
  }
}
