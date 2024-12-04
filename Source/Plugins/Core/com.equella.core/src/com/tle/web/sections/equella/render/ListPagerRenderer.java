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

package com.tle.web.sections.equella.render;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.PageUniqueId;
import com.tle.web.sections.js.generic.expression.ElementValueExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.AssignStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.HiddenInput;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.result.util.NumberLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.HtmlPagerState;
import com.tle.web.sections.standard.renderers.AbstractComponentRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import java.io.IOException;
import java.util.Map;

@SuppressWarnings("nls")
public class ListPagerRenderer extends AbstractComponentRenderer {
  private static final PluginResourceHelper RESOURCES =
      ResourcesService.getResourceHelper(ListPagerRenderer.class);

  private final HtmlPagerState page;

  private JSCallable changeHandler;
  private HiddenInput hiddenState;
  private ElementId hiddenId;

  public ListPagerRenderer(HtmlPagerState state) {
    super(state);
    addClass("paging");
    page = state;
    ListPagerRenderer previous = state.getAttribute(ListPagerRenderer.class);
    if (previous != null) {
      setElementId(new AppendedElementId(getWrappedElementId(), new PageUniqueId()));
      changeHandler = previous.changeHandler;
      hiddenId = previous.hiddenId;
    } else {
      state.setAttribute(ListPagerRenderer.class, this);
      hiddenId = new AppendedElementId(this, "_hid");
      hiddenState = new HiddenInput(hiddenId, page.getName(), Integer.toString(page.getCurrent()));
    }
  }

  @Override
  protected String getTag() {
    return "div";
  }

  @Override
  protected void writeMiddle(SectionWriter writer) throws IOException {
    if (hiddenState != null) {
      writer.render(hiddenState);
    }
    writer.writeTag("ul"); // $NON-NLS-1$
    int currentPage = page.getCurrent();
    int lastPage = page.getLastPage();
    int startPage = page.getStartPage();
    int endPage = page.getEndPage();

    if (startPage != endPage) {
      // write First and Prev
      if (currentPage > 1) {
        writeNav(writer, page.getPrevLabel(), "prev", currentPage - 1);
      }

      // write the links...
      for (int i = startPage; i <= endPage; i++) {
        writeNav(writer, new NumberLabel(i), Integer.toString(i), i);
      }

      // write Next and Last
      if (currentPage < lastPage) {
        writeNav(writer, page.getNextLabel(), "next", currentPage + 1);
      }
    }
    writer.endTag("ul");
  }

  protected void writeNav(SectionWriter writer, Label label, String key, int gotoPage)
      throws IOException {
    if (label == null) {
      label = new KeyLabel(RESOURCES.key("pager." + key));
    }

    HtmlLinkState nav = new HtmlLinkState();
    nav.setElementId(new AppendedElementId(this, key));
    if (page.getCurrent() == gotoPage) {
      nav.setDisabled(true);
    } else {
      nav.setClickHandler(new OverrideHandler(changeHandler, gotoPage));
    }

    LinkRenderer navLink = new LinkRenderer(nav);
    navLink.setNestedRenderable(new LabelRenderer(label));

    writer.writeTag("li");
    writer.render(navLink);

    // We need output this space to ensure that RTL languages like Arabic
    // will display correctly. If this isn't here, the Bidi algorithm
    // assumes all the pager numbers are one big number and displays them
    // LTR instead.
    writer.write(" ");

    writer.endTag("li");
  }

  @Override
  protected void processHandler(
      SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler) {
    if (!event.equals(JSHandler.EVENT_CHANGE)) {
      super.processHandler(writer, attrs, event, handler);
    } else {
      ScriptVariable scriptPage = new ScriptVariable("page");
      JSStatements body =
          StatementBlock.get(
              new AssignStatement(new ElementValueExpression(hiddenId), scriptPage), handler);
      changeHandler = new SimpleFunction("change", this, body, scriptPage);
    }
  }
}
