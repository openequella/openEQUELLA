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

package com.tle.web.sections.equella;

import com.tle.core.services.user.UserSessionService;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.events.BeforeEventsListener;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.ForwardEventListener;
import com.tle.web.sections.events.RenderEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.render.TemplateResultCollector;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.MenuMode;
import com.tle.web.viewurl.ViewItemUrl;
import javax.inject.Inject;

@TreeIndexed
public abstract class AbstractRootModalSessionSection<
        M extends AbstractRootModalSessionSection.RootModalSessionModel>
    extends AbstractPrototypeSection<M>
    implements ForwardEventListener, HtmlRenderer, BeforeEventsListener {
  @Inject protected UserSessionService sessionService;

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);
  }

  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    Throwable ex = context.getAttribute(AbstractModalSessionExceptionHandler.MODAL_ERROR_KEY);
    M model = getModel(context);
    model.setRendering(true);

    String rootId = context.getWrappedRootId(context);
    try {
      TemplateResultCollector wrappedResults = new TemplateResultCollector();
      context.processEvent(new RenderEvent(context, rootId, wrappedResults));
      model.setParts(wrappedResults.getTemplateResult());
    } catch (Exception t) {
      if (ex == null) {
        ex = t;
      }
      model.setParts(new GenericTemplateResult());
    }

    Decorations decorations = Decorations.getDecorations(context);
    decorations.clearAllDecorations();
    decorations.setMenuMode(MenuMode.HIDDEN);

    model.setSections(renderChildren(context, new TemplateResultCollector()).getTemplateResult());

    if (ex != null) {
      context.setAttribute(SectionInfo.KEY_ORIGINAL_EXCEPTION, ex);
      context.setAttribute(SectionInfo.KEY_MATCHED_EXCEPTION, ex);
      model.setParts(renderToTemplate(context, getErrorSection().getSectionId()));
    }

    setupModelForRender(context, model);

    return getFinalRenderable(context, model);
  }

  public void setSessionId(SectionInfo info, String sessionId) {
    RootModalSessionModel model = getModel(info);
    model.setStateId(sessionId);
  }

  public String getSessionId(SectionInfo info) {
    RootModalSessionModel model = getModel(info);
    return model.getStateId();
  }

  @Override
  public void forwardCreated(SectionInfo info, SectionInfo forward) {
    if (!forward.getBooleanAttribute(ModalSession.KEY_IGNORE_CURRENT_SESSION)) {
      String stateId = getSessionId(info);
      if (stateId != null) {
        setSessionId(forward, stateId);
      }
    }
  }

  private void checkState(SectionInfo info, String sessionId) {
    if (sessionId != null && info.getAttribute(getSessionKey()) == null) {
      Object sessionObject = sessionService.getAttribute(sessionId);
      if (sessionObject != null) {
        info.setAttribute(getSessionKey(), sessionObject);
      }
    }
  }

  @Override
  public void beforeEvents(SectionInfo info) {
    MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
    SectionTree tree = getTree();
    if (minfo.getAttribute(getSessionKey()) == null) {
      minfo.removeTree(tree);
    } else {
      minfo.queueTreeEvents(tree);
    }
  }

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new RootModalSessionModel(info, this);
  }

  public static class RootModalSessionModel {
    @Bookmarked(
        ignoreForContext = {ViewItemUrl.VIEWONLY_CONTEXT},
        contexts = {BookmarkEvent.CONTEXT_SESSION})
    private String stateId;

    @Bookmarked(
        name = "t",
        ignoreForContext = {ViewItemUrl.VIEWONLY_CONTEXT},
        contexts = {"TEMP"})
    private boolean noTemplate;

    private boolean rendering;
    private TemplateResult sections;
    private TemplateResult parts;
    private final SectionInfo info;
    private final AbstractRootModalSessionSection<?> section;

    public RootModalSessionModel(SectionInfo info, AbstractRootModalSessionSection<?> section) {
      this.info = info;
      this.section = section;
    }

    public String getStateId() {
      return stateId;
    }

    public void setStateId(String stateId) {
      this.stateId = stateId;
      section.checkState(info, stateId);
    }

    public boolean isNoTemplate() {
      return noTemplate;
    }

    public void setNoTemplate(boolean noTemplate) {
      this.noTemplate = noTemplate;
    }

    public TemplateResult getSections() {
      return sections;
    }

    public void setSections(TemplateResult sections) {
      this.sections = sections;
    }

    public TemplateResult getParts() {
      return parts;
    }

    public void setParts(TemplateResult parts) {
      this.parts = parts;
    }

    public boolean isRendering() {
      return rendering;
    }

    public void setRendering(boolean rendering) {
      this.rendering = rendering;
    }
  }

  protected abstract Object getSessionKey();

  protected abstract SectionId getErrorSection();

  protected abstract void setupModelForRender(SectionInfo info, M model);

  protected abstract SectionResult getFinalRenderable(RenderEventContext context, M model);
}
