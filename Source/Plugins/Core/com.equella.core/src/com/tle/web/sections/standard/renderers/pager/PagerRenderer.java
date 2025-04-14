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

package com.tle.web.sections.standard.renderers.pager;

import com.tle.common.Check;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.PageUniqueId;
import com.tle.web.sections.js.generic.expression.ElementValueExpression;
import com.tle.web.sections.js.generic.statement.AssignStatement;
import com.tle.web.sections.render.HiddenInput;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.HtmlPagerState;
import com.tle.web.sections.standard.renderers.AbstractComponentRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class PagerRenderer extends AbstractComponentRenderer {
  private static final String ALREADY_RENDERED = "PagerRenderer"; // $NON-NLS-1$
  private static final PluginResourceHelper RESOURCES =
      ResourcesService.getResourceHelper(PagerRenderer.class);

  private final HtmlPagerState page;

  private JSHandler userClickHandler;
  private String currentPageCssClass;
  private String otherPageCssClass;
  private HiddenInput hiddenState;

  public PagerRenderer(HtmlPagerState state) {
    super(state);
    page = state;
    if (state.getAttribute(ALREADY_RENDERED) != null) {
      setElementId(new AppendedElementId(getWrappedElementId(), new PageUniqueId()));
    } else {
      state.setAttribute(ALREADY_RENDERED, true);
      hiddenState = new HiddenInput(this, page.getName(), Integer.toString(page.getCurrent()));
    }
  }

  @Override
  protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException {
    if (hiddenState != null) {
      writer.render(hiddenState);
    }
    int currentPage = page.getCurrent();
    int lastPage = page.getLastPage();
    int startPage = page.getStartPage();
    int endPage = page.getEndPage();

    if (startPage != endPage) {
      // write First and Prev
      if (currentPage > 1) {
        writeNav(writer, page.getFirstLabel(), "first", 1); // $NON-NLS-1$
        writeNav(writer, page.getPrevLabel(), "prev", currentPage - 1); // $NON-NLS-1$
        writer.write("&nbsp;|&nbsp;"); // $NON-NLS-1$
      }

      // write the links...
      for (int i = startPage; i <= endPage; i++) {
        String pageNum = Integer.toString(i);

        HtmlLinkState p = new HtmlLinkState();
        p.setElementId(new AppendedElementId(this, "p" + pageNum)); // $NON-NLS-1$
        p.setClickHandler(pageChangeHandler(writer, i));
        p.setLabel(new TextLabel(pageNum));

        LinkRenderer rend = new LinkRenderer(p);
        if (i == currentPage) {
          rend.setStyles(null, getCurrentPageCssClass(), null);
        } else {
          rend.setStyles(null, getOtherPageCssClass(), null);
        }
        rend.realRender(writer);
        writer.write("&nbsp;"); // $NON-NLS-1$
      }

      // write Next and Last
      if (currentPage < lastPage) {
        writer.write("|&nbsp;"); // $NON-NLS-1$
        writeNav(writer, page.getNextLabel(), "next", currentPage + 1); // $NON-NLS-1$
        writeNav(writer, page.getLastLabel(), "last", lastPage); // $NON-NLS-1$
      }
    }
  }

  protected JSHandler pageChangeHandler(SectionInfo info, int pageNumber) {
    return new OverrideHandler(
        new AssignStatement(new ElementValueExpression(this), pageNumber), userClickHandler);
  }

  protected void writeNav(SectionWriter writer, Label label, String key, int gotoPage)
      throws IOException {
    int currentPage = page.getCurrent();

    String navText = (label == null ? null : label.getText());
    if (navText == null) {
      navText = RESOURCES.getString("pager." + key); // $NON-NLS-1$
    }
    StringWriter imageAndText = new StringWriter();
    SectionWriter s = new SectionWriter(imageAndText, writer);
    if (currentPage > gotoPage) {
      writeNavImage(s, key, navText);
      s.write("&nbsp;" + navText); // $NON-NLS-1$
    } else {
      s.write(navText + "&nbsp;"); // $NON-NLS-1$
      writeNavImage(s, key, navText);
    }

    HtmlLinkState nav = new HtmlLinkState();
    nav.setElementId(new AppendedElementId(this, key));
    nav.setClickHandler(pageChangeHandler(writer, gotoPage));

    LinkRenderer navLink = new LinkRenderer(nav);
    navLink.setNestedRenderable(new SimpleSectionResult(imageAndText));
    navLink.realRender(writer);
  }

  protected void writeNavImage(SectionWriter writer, String key, String alt) throws IOException {
    ImageRenderer img =
        new ImageRenderer(
            "images/go" + key + "icon.gif", new TextLabel(alt)); // $NON-NLS-1$ //$NON-NLS-2$
    img.setStyles("border: none;", null, null); // $NON-NLS-1$
    img.realRender(writer);
  }

  @Override
  protected void processHandler(
      SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler) {
    if (!event.equals(JSHandler.EVENT_CHANGE)) {
      super.processHandler(writer, attrs, event, handler);
    } else {
      userClickHandler = handler;
    }
  }

  /** No tag. Wrap with your favourite enclosing tag if you wish. */
  @Override
  protected String getTag() {
    return null;
  }

  @Override
  protected void writeEnd(SectionWriter writer) throws IOException {
    // it has no tag
  }

  @Override
  public void preRender(PreRenderContext info) {
    super.preRender(info);
    info.addCss(RESOURCES.url("css/pager.css")); // $NON-NLS-1$
  }

  public String getCurrentPageCssClass() {
    if (Check.isEmpty(currentPageCssClass)) {
      return "current"; //$NON-NLS-1$
    }
    return currentPageCssClass;
  }

  public void setCurrentPageCssClass(String currentPageCssClass) {
    this.currentPageCssClass = currentPageCssClass;
  }

  public String getOtherPageCssClass() {
    return otherPageCssClass;
  }

  public void setOtherPageCssClass(String otherPageCssClass) {
    this.otherPageCssClass = otherPageCssClass;
  }
}
