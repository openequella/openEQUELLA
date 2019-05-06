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

import com.tle.common.Check;
import com.tle.web.sections.SectionWriter;
import java.io.IOException;
import java.util.Map;

public class WrappedLabelRenderer extends TagRenderer {
  private final WrappedLabel label;
  private String text;

  public WrappedLabelRenderer(WrappedLabel label) {
    super(label.isInline() ? "span" : "div", new TagState());
    this.label = label;
    addClass("wrapped");
  }

  @Override
  protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs)
      throws IOException {
    if (label.isShowAltText()) {
      attrs.put("title", label.getUnprocessedLabel().getText());
    }
    super.prepareFirstAttributes(writer, attrs);
  }

  @Override
  protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException {
    text = label.getText();
    if (!Check.isEmpty(text)) {
      super.writeStart(writer, attrs);
    }
  }

  @Override
  protected void writeMiddle(SectionWriter writer) throws IOException {
    if (!Check.isEmpty(text)) {
      if (label.isHtml()) {
        writer.write(label.getText());
      } else {
        writer.writeText(label.getText());
      }
    }
  }

  @Override
  protected void writeEnd(SectionWriter writer) throws IOException {
    if (!Check.isEmpty(text)) {
      super.writeEnd(writer);
    }
  }
}
