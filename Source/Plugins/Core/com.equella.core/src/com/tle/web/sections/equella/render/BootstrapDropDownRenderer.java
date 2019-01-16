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

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.AppendedLabel;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.LinkRenderer;

@SuppressWarnings("nls")
public class BootstrapDropDownRenderer extends AbstractFauxDropDownRenderer {
  private static final Label CARET_LABEL =
      new TextLabel("&nbsp;<span class=\"caret\"></span>", true);
  private static final Label ACTIVE_CARET_LABEL =
      new TextLabel("<span class=\"caret\"></span>", true);

  public static final String RENDER_CONSTANT = "bootstrapdropdown";
  public static final String ACTIVE_RENDER_CONSTANT = "bootstrapactivedropdown";
  protected final HtmlLinkState triggerLink;
  protected Set<String> dropdownStyleClasses;

  private final boolean active;

  public BootstrapDropDownRenderer(HtmlListState state, boolean active) {
    super(state);
    verifyState(state);
    triggerLink = new HtmlLinkState();
    this.active = active;
  }

  protected void verifyState(HtmlListState state) {
    if (state.getLabel() == null) {
      throw new RuntimeException(
          getClass()
              + " requires that a label is set on the component or state to show as the trigger");
    }
  }

  public BootstrapDropDownRenderer addDropdownClass(String extraClass) {
    if (extraClass != null) {
      if (dropdownStyleClasses == null) {
        dropdownStyleClasses = new LinkedHashSet<String>();
      }
      dropdownStyleClasses.add(extraClass);
    }
    return this;
  }

  protected Set<String> getDropdownStyleClasses() {
    return dropdownStyleClasses;
  }

  protected String getDropdownStyleClassesString() {
    String extra = "";
    if (!Check.isEmpty(dropdownStyleClasses)) {
      extra = " " + Utils.join(dropdownStyleClasses.toArray(), " ");
    }
    return "dropdown-menu" + extra;
  }

  @Override
  protected String getTag() {
    return null;
  }

  @Override
  protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException {
    // Do nothing
  }

  @Override
  protected void writeEnd(SectionWriter writer) throws IOException {
    // Do nothing
  }

  @Override
  protected void writeMiddle(SectionWriter writer) throws IOException {
    writeTrigger(writer);
    writeUl(writer);
    super.writeMiddle(writer);
  }

  protected void writeUl(SectionWriter writer) throws IOException {
    writer.writeTag(
        "ul",
        "class",
        getDropdownStyleClassesString(),
        "role",
        "menu",
        "aria-labelledby",
        triggerLink.getElementId(writer));
    writeOptions(writer);
    writer.endTag("ul");
  }

  protected void writeTrigger(SectionWriter writer) throws IOException {
    // The link that shows for the drop-down
    if (active) {
      triggerLink.addClass("active");
    }
    final Label triggerLabel = state.getLabel();
    triggerLink.setLabel(
        active ? ACTIVE_CARET_LABEL : AppendedLabel.get(triggerLabel, CARET_LABEL));
    triggerLink.setTitle(triggerLabel);
    triggerLink.addClass("dropdown-toggle");
    triggerLink.addClass("btn");
    triggerLink.addTagProcessor(Bootstrap.TOGGLE_ATTR);
    writer.render(new ButtonRenderer(triggerLink));
  }

  protected void writeOptions(SectionWriter writer) throws IOException {
    for (Option<?> op : options) {
      final HtmlLinkState opLink = new HtmlLinkState(new TextLabel(op.getName(), op.isNameHtml()));
      if (op.hasAltTitleAttr()) {
        opLink.setTitle(new TextLabel(op.getAltTitleAttr()));
      }

      final String value = op.getValue();
      opLink.setClickHandler(new OverrideHandler(clickFunc, value));

      if (value.equalsIgnoreCase(getSelectedValue())) {
        writer.writeTag("li", "class", "active");
      } else {
        writer.writeTag("li");
      }
      writer.render(new LinkRenderer(opLink));
      writer.endTag("li");
    }
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(Bootstrap.PRERENDER);
    super.preRender(info);
  }

  @Override
  public JSCallable createSetAllFunction() {
    throw new UnsupportedOperationException("Not yet");
  }
}
