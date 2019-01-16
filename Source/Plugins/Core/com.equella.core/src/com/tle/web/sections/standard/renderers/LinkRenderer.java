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
import java.util.Map;

import com.tle.common.Check;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.validators.FunctionCallValidator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;

public class LinkRenderer extends AbstractComponentRenderer implements LinkTagRenderer {
  private static final IncludeFile INCLUDE =
      new IncludeFile(
          ResourcesService.getResourceHelper(LinkRenderer.class).url("scripts/render/link.js"));
  private static final ExternallyDefinedFunction FUNCTION_IS_ENABLED =
      new ExternallyDefinedFunction("isEnabled", INCLUDE);
  private static final ExternallyDefinedFunction FUNCTION_SET_DISABLED =
      new ExternallyDefinedFunction("setDisabled", INCLUDE);

  protected HtmlLinkState linkState;
  private String target;
  private String rel;
  private Label title;
  protected boolean disabled;
  protected boolean disablable;
  private boolean ensureClickable;
  private StatementHandler clickHandler;

  private SimpleFunction disableFunc;

  public LinkRenderer(HtmlLinkState state) {
    super(state);
    this.linkState = state;
    this.target = state.getTarget();
    this.rel = state.getRel();
    this.title = state.getTitle();
    this.disabled = state.isDisabled();
    this.disablable = state.isDisablable();

    if (disablable) {
      JSHandler handler = state.getHandler(JSHandler.EVENT_CLICK);
      clickHandler = new OverrideHandler();
      clickHandler.addValidator(new FunctionCallValidator(FUNCTION_IS_ENABLED, Jq.$(this)));
      if (handler != null) {
        clickHandler.addStatements(handler.getStatements());
        clickHandler.addValidator(handler.getValidators());
      }
    }
  }

  @Override
  protected void processHandler(
      SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler) {
    if (!disablable || !event.equals(JSHandler.EVENT_CLICK)) {
      super.processHandler(writer, attrs, event, handler);
    }
  }

  public LinkRenderer(HtmlComponentState state) {
    super(state);
    this.linkState = null;
    this.disabled = state.isDisabled();
    setTitle(state.getLabel());
  }

  @Override
  public HtmlLinkState getLinkState() {
    return linkState;
  }

  @Override
  public boolean isDisabled() {
    return disabled;
  }

  @Override
  protected boolean isStillAddClickHandler() {
    return false;
  }

  @SuppressWarnings("nls")
  @Override
  protected String getTag() {
    return (!disabled || disablable) ? "a" : "span";
  }

  @SuppressWarnings("nls")
  @Override
  protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs)
      throws IOException {
    if (!disabled || disablable) {
      attrs.put("href", getHref(writer));
    }

    attrs.put("title", labelOrNull(title));
    if (disabled && disablable) {
      addClass(attrs, "disabled");
      attrs.put("aria-disabled", "true");
    }

    attrs.put("target", target);
    attrs.put("rel", getRel());
    if (clickHandler != null) {
      writer.bindHandler(JSHandler.EVENT_CLICK, attrs, clickHandler);
    }
  }

  private String labelOrNull(Label label) {
    if (label == null) {
      return null;
    }
    String text = label.getText();
    return Check.isEmpty(text) ? null : text;
  }

  public String getRel() {
    return rel;
  }

  @Override
  protected void prepareLastAttributes(SectionWriter writer, Map<String, String> attrs) {
    if (ensureClickable && !attrs.containsKey("on" + JSHandler.EVENT_CLICK)) // $NON-NLS-1$
    {
      // this is not very pretty...
      if (target != null && target.equals("_blank")) // $NON-NLS-1$
      {
        attrs.put("onclick", "window.open(this.href); return false;"); // $NON-NLS-1$//$NON-NLS-2$
      } else {
        attrs.put(
            "onclick",
            "document.location.href=this.href; return false;"); //$NON-NLS-1$//$NON-NLS-2$
      }
    }
  }

  @SuppressWarnings("nls")
  protected String getHref(SectionInfo info) {
    String href = "javascript:void(0);";
    if (linkState != null) {
      Bookmark bookmark = linkState.getBookmark();
      if (bookmark != null) {
        href = bookmark.getHref();
      }
    }
    return href;
  }

  @SuppressWarnings("nls")
  @Override
  public JSCallable createDisableFunction() {
    if (disableFunc == null) {
      final ScriptVariable dis = new ScriptVariable("dis");
      disableFunc =
          new SimpleFunction("dis", this, Js.call_s(FUNCTION_SET_DISABLED, Jq.$(this), dis), dis);
    }

    return disableFunc;
  }

  @Override
  public void setRel(String rel) {
    this.rel = rel;
  }

  @Override
  public void setTarget(String target) {
    this.target = target;
  }

  @Override
  public void setTitle(Label title) {
    this.title = title;
  }

  @Override
  public void setLabel(Label label) {
    setNestedRenderable(new LabelRenderer(label));
  }

  @Override
  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  @Override
  public void ensureClickable() {
    this.ensureClickable = true;
  }
}
