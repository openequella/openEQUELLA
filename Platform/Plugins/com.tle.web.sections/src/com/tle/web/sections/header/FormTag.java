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

package com.tle.web.sections.header;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.HiddenInput;
import com.tle.web.sections.render.NestedRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;

public class FormTag extends TagState implements NestedRenderable {
  private NestedRenderable renderer;
  protected String method = "POST"; // $NON-NLS-1$
  protected String encoding;
  protected String name;
  protected FormAction action;
  private List<HiddenInput> inputs = Lists.newArrayList();

  public FormTag() {
    renderer = new FormTagRenderer();
  }

  @Override
  public SectionRenderable getNestedRenderable() {
    return renderer.getNestedRenderable();
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void realRender(SectionWriter writer) throws IOException {
    renderer.realRender(writer);
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(renderer);
  }

  @Override
  public NestedRenderable setNestedRenderable(SectionRenderable nested) {
    renderer.setNestedRenderable(nested);
    return this;
  }

  public class FormTagRenderer extends BufferedTagRenderer {
    public FormTagRenderer() {
      super("form", FormTag.this); // $NON-NLS-1$
    }

    @SuppressWarnings("nls")
    @Override
    protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs)
        throws IOException {
      super.prepareFirstAttributes(writer, attrs);
      attrs.put("name", name);
      attrs.put("method", method);
      attrs.put("enctype", encoding);
      attrs.put("action", action.getFormAction());
    }

    @SuppressWarnings("nls")
    @Override
    protected void writeMiddle(SectionWriter writer) throws IOException {
      writer.writeTag("div", "style", "display:none;", "class", "_hiddenstate");
      writer.render(new HiddenInput(action.getHiddenState()));
      for (HiddenInput hidden : inputs) {
        writer.render(hidden);
      }
      writer.endTag("div");
      super.writeMiddle(writer);
    }
  }

  public NestedRenderable getRenderer() {
    return renderer;
  }

  public void setRenderer(NestedRenderable renderer) {
    this.renderer = renderer;
  }

  public FormAction getAction() {
    return action;
  }

  public void setAction(FormAction action) {
    this.action = action;
  }

  public void addHidden(HiddenInput hidden) {
    inputs.add(hidden);
  }
}
