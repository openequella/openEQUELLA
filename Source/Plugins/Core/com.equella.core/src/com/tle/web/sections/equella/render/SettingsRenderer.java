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

package com.tle.web.sections.equella.render;

import static com.tle.web.sections.render.CssInclude.include;

import java.io.IOException;

import com.tle.common.Check;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;

/**
 * Perhaps add support for a SettingState ?
 *
 * @author Aaron
 */
@SuppressWarnings("nls")
public class SettingsRenderer extends TagRenderer {
  private static final CssInclude SETTINGS_CSS =
      include(ResourcesService.getResourceHelper(SettingsRenderer.class).url("css/settings.css"))
          .hasRtl()
          .make();

  private final Label label;

  public SettingsRenderer(Label label, SectionRenderable contents, String extraClass) {
    super("div", new TagState());
    this.label = label;
    addClass("settingRow");
    if (!Check.isEmpty(extraClass)) {
      addClass(extraClass);
    }
    setNestedRenderable(contents);
  }

  @Override
  protected void writeMiddle(SectionWriter writer) throws IOException {
    writer.writeTag("div", "class", "settingLabel");
    writer.render(new LabelRenderer(label));
    writer.endTag("div");

    writer.writeTag("div", "class", "settingField");

    writer.writeTag("div");
    writer.render(getNestedRenderable());
    writer.endTag("div");

    writer.endTag("div");
  }

  @Override
  public void preRender(PreRenderContext info) {
    super.preRender(info);
    info.preRender(SETTINGS_CSS);
  }
}
