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

package com.tle.web.template;

import static com.tle.web.sections.render.CssInclude.include;

import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LocaleUtils;
import com.tle.core.accessibility.AccessibilityModeService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.DebugSettings;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.navigation.MenuService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.equella.js.StandardExpressions;
import com.tle.web.sections.equella.layout.InnerLayout;
import com.tle.web.sections.equella.layout.LayoutSelector;
import com.tle.web.sections.equella.layout.OuterLayout;
import com.tle.web.sections.equella.render.Bootstrap;
import com.tle.web.sections.events.BeforeEventsListener;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.events.ForwardEventListener;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.StandardRenderContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.header.BodyTag;
import com.tle.web.sections.header.FormTag;
import com.tle.web.sections.header.InfoFormAction;
import com.tle.web.sections.header.MutableHeaderHelper;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.jquery.libraries.JQueryDraggable;
import com.tle.web.sections.jquery.libraries.JQueryResizable;
import com.tle.web.sections.jquery.libraries.JQueryTimer;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.*;
import com.tle.web.sections.render.CssInclude.Priority;
import com.tle.web.template.Decorations.MenuMode;
import com.tle.web.template.section.HeaderSection;
import com.tle.web.template.section.HtmlStyleClass;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("nls")
public class RenderTemplate extends AbstractPrototypeSection<RenderTemplate.RenderTemplateModel>
    implements HtmlRenderer, ForwardEventListener, BeforeEventsListener {
  private static final Log LOGGER = LogFactory.getLog(RenderTemplate.class);

  public static final String XSRF_PARAM = "__xsrf";

  private static final PluginResourceHelper RESOURCES =
      ResourcesService.getResourceHelper(RenderTemplate.class);

  private static final CssInclude BOOTSTRAP_CSS = Bootstrap.CSS;
  public static final CssInclude STYLES_CSS =
      include(RESOURCES.url("css/styles.css"))
          .prerender(BOOTSTRAP_CSS)
          .hasRtl()
          .priority(Priority.LOWEST)
          .make();
  public static final CssInclude IE11_COMPAT_CSS =
      include(RESOURCES.url("css/ie11compat.css")).hasRtl().priority(Priority.LOWEST).make();
  public static final CssInclude TINYMCE_SKIN_CSS =
      include(RESOURCES.url("reactjs/tinymce/skins/ui/oxide/skin.css"))
          .hasRtl()
          .priority(Priority.LOWEST)
          .make();
  public static final CssInclude TINYMCE_CONTENT_CSS =
      include(RESOURCES.url("reactjs/tinymce/skins/ui/oxide/content.css"))
          .hasRtl()
          .priority(Priority.LOWEST)
          .make();
  public static final CssInclude TINYMCE_CONTENT_MIN_CSS =
      include(RESOURCES.url("reactjs/tinymce/skins/ui/oxide/content.min.css"))
          .hasRtl()
          .priority(Priority.LOWEST)
          .make();
  public static final CssInclude CUSTOMER_CSS =
      include("css/customer.css").priority(Priority.HIGHEST).make();

  private static final String KEY_IGNORE_STANDARD_TEMPLATE = "IGNORE_STANDARD_TEMPLATE";
  private static final IncludeFile HEARTBEAT =
      new IncludeFile(
          RESOURCES.url("scripts/heartbeat.js"), JQueryCore.JQUERY, JQueryTimer.PRERENDER);
  public static final IncludeFile AUTOTEST_JS =
      new IncludeFile(RESOURCES.url("scripts/autotest.js"), JQueryCore.JQUERY);
  private static final IncludeFile DEBUG_JS =
      new IncludeFile(RESOURCES.url("scripts/debug.js"), JQueryCore.PRERENDER);
  private static final IncludeFile DEBUG_AARON_JS =
      new IncludeFile(
          RESOURCES.url("scripts/debug_aaron.js"),
          DEBUG_JS,
          include(RESOURCES.url("css/debug.css")).make(),
          JQueryDraggable.PRERENDER,
          JQueryResizable.PRERENDER);

  @ViewFactory private FreemarkerFactory viewFactory;

  @Inject private MenuService menuService;
  @Inject private HeaderSection header;
  @Inject private AccessibilityModeService acMode;
  @Inject private PluginTracker<HtmlStyleClass> htmlCssClassTracker;

  /**
   * Checks whether the context attribute new UI has been disabled or if the URL contains
   * "DISABLE_NEWUI". The "DISABLE_NEWUI" parameter serves as a temporary disable, specifically
   * targeted for a single request, such as a request to download the theme package in the new UI.
   */
  private boolean isNewUIDisabled(RenderEventContext context) {
    return context.getBooleanAttribute(RenderNewTemplate.DisableNewUI())
        || Optional.ofNullable(context.getParameterMap().get(RenderNewTemplate.DisableNewUI()))
            .map(arr -> arr[0])
            .map(Boolean::parseBoolean)
            .orElse(false);
  }

  @Override
  public SectionResult renderHtml(RenderEventContext context) throws Exception {
    boolean oldLayout = !RenderNewTemplate.isNewLayout(context) || isNewUIDisabled(context);
    setupHeaderHelper(context);
    if (checkForResponse(context)) {
      return null;
    }
    if (!oldLayout && context.getAttributeForClass(AjaxRenderContext.class) == null) {
      return RenderNewTemplate.renderNewHtml(context, viewFactory);
    }
    RenderTemplateModel model = getModel(context);

    TemplateResultCollector collector = new TemplateResultCollector();
    TemplateResult template;
    SectionResult body = context.getRenderedBody();
    if (body != null) {
      collector.returnResult(body, "");
      if (context.isRenderHeader()) {
        SectionUtils.renderSection(context, header, collector);
      }
      template = collector.getTemplateResult();
    } else {
      Decorations decorations = setupDecorations(context, model);

      String renderId = context.getModalId();
      if (renderId == null) {
        renderId = context.getWrappedRootId(context);
      }
      context.processEvent(new RenderEvent(context, renderId, collector));
      if (context.isRendered() || checkForResponse(context)) {
        return null;
      }

      if (context.getTreeAttribute(KEY_IGNORE_STANDARD_TEMPLATE) == null) {
        model.setTemplate(collector.getTemplateResult());
        renderChildren(context, collector);
      }

      if (context.isRenderHeader()) {
        SectionUtils.renderSection(context, header, collector);
      }

      // Needs to happen after rendering children to allow them to set
      // whether it is full-screen or not.
      setHtmlAttrsToModel(context, model, decorations);

      template = collector.getTemplateResult();
      template = selectInnerLayout(context, template);
    }

    PreRenderContext precontext = context.getPreRenderContext();
    model.getBody().setPostmarkup(template.getNamedResult(context, "postmarkup"));
    precontext.preRender(STYLES_CSS);
    precontext.preRender(CUSTOMER_CSS);
    return selectLayout(context, template);
  }

  private void setupHeaderHelper(RenderEventContext context) {
    MutableHeaderHelper helper = (MutableHeaderHelper) context.getHelper();

    RenderTemplateModel model = getModel(context);

    model.setLang(LocaleUtils.toHtmlLang(CurrentLocale.getLocale()));
    model.setRightToLeft(CurrentLocale.isRightToLeft());
    helper.setElementFunction(StandardExpressions.ELEMENT_FUNCTION);

    BodyTag bodyTag = context.getBody();
    FormTag formTag = context.getForm();
    if (helper.getFormExpression() == null) {
      formTag.setId(StandardExpressions.FORM_NAME);
      helper.setFormExpression(StandardExpressions.FORM_EXPRESSION);
    }

    if (!helper.isSubmitFunctionsSet()) {
      helper.setSubmitFunctions(
          StandardExpressions.SUBMIT_EVENT_FUNCTION,
          StandardExpressions.SUBMIT_EVENT_NOVAL_FUNCTION,
          StandardExpressions.SUBMIT_FUNCTION,
          StandardExpressions.SUBMIT_NOVAL_FUNCTION);
    }
    helper.setTriggerEventFunction(StandardExpressions.TRIGGER_EVENT_FUNCTION);
    StandardRenderContext standardContext =
        context.getAttributeForClass(StandardRenderContext.class);
    standardContext.setBindFunction(StandardExpressions.BIND_EVENT_FUNCTION);
    standardContext.setBindW3CFunction(StandardExpressions.BIND_W3C_FUNCTION);
    if (formTag.getAction() == null) {
      BookmarkEvent bookmarkEvent = new BookmarkEvent(null, true, null);
      formTag.setAction(new InfoFormAction(new InfoBookmark(context, bookmarkEvent)));
    }

    model.setForm(
        Decorations.getDecorations(context).isExcludeForm()
            ? new ContentOnlyRenderable()
            : formTag);
    model.setBody(bodyTag);
  }

  private Decorations setupDecorations(RenderEventContext context, RenderTemplateModel model) {
    Decorations decorations = Decorations.getDecorations(context);
    if (context.getAttributeForClass(AjaxRenderContext.class) != null) {
      decorations.setBanner(false);
    } else {
      if (model.isHideNav()) {
        decorations.setMenuMode(MenuMode.HIDDEN);
      }
      decorations.setBanner(!model.isHideBanner());
    }

    List<LayoutSelector> layoutSelectors = InnerLayout.getLayoutSelectors(context);
    for (LayoutSelector layoutSelector : layoutSelectors) {
      layoutSelector.preProcess(decorations);
    }

    return decorations;
  }

  private void setHtmlAttrsToModel(
      RenderEventContext context, RenderTemplateModel model, Decorations decorations) {
    StringBuilder sb = new StringBuilder();
    switch (decorations.isFullscreen()) {
      case YES_WITH_TOOLBAR:
        sb.append("fullscreen-toolbar ");
        break;
      case YES:
        sb.append("fullscreen ");
        break;

      default:
        break;
    }

    if (acMode.isAccessibilityMode()) {
      sb.append("accessibility ");
    }

    for (HtmlStyleClass classProvider : htmlCssClassTracker.getBeanList()) {
      if (!Check.isEmpty(classProvider.getStyleClass(context))) {
        sb.append(classProvider.getStyleClass(context) + " ");
      }
    }

    if (sb.length() > 0) {
      sb.insert(0, "class=\"");
      sb.append('"');
      model.setHtmlAttrs(sb.toString());
    }
  }

  private boolean checkForResponse(RenderEventContext context) {
    SectionRenderable response = context.getRenderedResponse();
    if (response != null) {
      context.getRenderEvent().returnResult(response);
      return true;
    }
    return false;
  }

  @Override
  public String getDefaultPropertyName() {
    // Do not ever change
    return "temp";
  }

  public static void addFormSubmitBinding(FormTag form) {
    form.addReadyStatements(
        new JQueryStatement(
            form, "bind('submit', function(){if (!g_bSubmitting) return false; })"));
  }

  private TemplateResult selectInnerLayout(RenderContext info, TemplateResult templateResult)
      throws Exception {
    PreRenderContext preRenderer = info.getPreRenderContext();
    preRenderer.preRender(HEARTBEAT);
    if (DebugSettings.isAutoTestMode()) {
      preRenderer.preRender(AUTOTEST_JS);
    }
    if (DebugSettings.isDebugAaron()) {
      preRenderer.preRender(DEBUG_AARON_JS);
    } else if (DebugSettings.isDebuggingMode()) {
      preRenderer.preRender(DEBUG_JS);
    }
    preRenderer.preRender(StandardExpressions.SUBMIT_JS);
    FormTag form = info.getRootRenderContext().getForm();
    addFormSubmitBinding(form);

    final Decorations decorations = Decorations.getDecorations(info);
    for (LayoutSelector layoutSelector : InnerLayout.getLayoutSelectors(info)) {
      TemplateResult layout = layoutSelector.getLayout(decorations, info, templateResult);
      if (layout != null) {
        return new FallbackTemplateResult(layout, templateResult);
      }
    }

    if (decorations.isBanner() || !decorations.isMenuHidden() || decorations.isContent()) {
      return new FallbackTemplateResult(
          viewFactory.createTemplateResultWithModel(
              InnerLayout.getLayout(info).getFtl(),
              new InnerLayoutModel(
                  templateResult,
                  decorations,
                  decorations.isBreadcrumbs() ? Breadcrumbs.get(info) : null)),
          templateResult);
    }
    return templateResult;
  }

  private SectionResult selectLayout(RenderContext info, TemplateResult result) {
    getModel(info).setTemplate(result);
    return viewFactory.createResult(OuterLayout.getLayout(info).getFtl(), this);
  }

  @Override
  public Class<RenderTemplateModel> getModelClass() {
    return RenderTemplateModel.class;
  }

  public TemplateResult getCurrentTemplate(SectionInfo info) {
    return getModel(info).getTemplate();
  }

  public static class InnerLayoutModel {
    private final TemplateResult template;
    private final Decorations decorations;
    private final Breadcrumbs breadcrumbs;

    public InnerLayoutModel(
        TemplateResult template, Decorations decorations, Breadcrumbs breadcrumbs) {
      this.template = template;
      this.decorations = decorations;
      this.breadcrumbs = breadcrumbs;
    }

    public TemplateResult getTemplate() {
      return template;
    }

    public Decorations getDecorations() {
      return decorations;
    }

    public Breadcrumbs getBreadcrumbs() {
      return breadcrumbs;
    }
  }

  public static class RenderTemplateModel {
    private TemplateResult template;
    private Decorations decorations;
    private String htmlAttrs = "";
    private SectionRenderable form;
    private BodyTag body;

    // Do not ever change
    @Bookmarked(name = "hn", contexts = BookmarkEvent.CONTEXT_SESSION)
    private boolean hideNav;

    // Do not ever change
    @Bookmarked(name = "hb", contexts = BookmarkEvent.CONTEXT_SESSION)
    private boolean hideBanner;

    private String lang;
    private boolean rightToLeft;

    public SectionRenderable getForm() {
      return form;
    }

    public void setForm(SectionRenderable form) {
      this.form = form;
    }

    public BodyTag getBody() {
      return body;
    }

    public void setBody(BodyTag body) {
      this.body = body;
    }

    public boolean isHideNav() {
      return hideNav;
    }

    public void setHideNav(boolean hideNav) {
      this.hideNav = hideNav;
    }

    public boolean isHideBanner() {
      return hideBanner;
    }

    public void setHideBanner(boolean hideBanner) {
      this.hideBanner = hideBanner;
    }

    public String getLang() {
      return lang;
    }

    public void setLang(String lang) {
      this.lang = lang;
    }

    public boolean isRightToLeft() {
      return rightToLeft;
    }

    public void setRightToLeft(boolean rightToLeft) {
      this.rightToLeft = rightToLeft;
    }

    public Decorations getDecorations() {
      return decorations;
    }

    public void setDecorations(Decorations decorations) {
      this.decorations = decorations;
    }

    public TemplateResult getTemplate() {
      return template;
    }

    public void setTemplate(TemplateResult template) {
      this.template = template;
    }

    public String getHtmlAttrs() {
      return htmlAttrs;
    }

    public void setHtmlAttrs(String htmlAttrs) {
      this.htmlAttrs = htmlAttrs;
    }
  }

  @Override
  public void forwardCreated(SectionInfo info, SectionInfo forward) {
    RenderTemplate temp = forward.lookupSection(RenderTemplate.class);
    if (temp != null) {
      RenderTemplateModel oldModel = getModel(info);
      RenderTemplateModel tempModel = temp.getModel(forward);
      tempModel.setHideNav(oldModel.isHideNav());
      tempModel.setHideBanner(oldModel.isHideBanner());
    }
  }

  public void setHideNavigation(SectionInfo info, boolean b) {
    getModel(info).setHideNav(b);
  }

  public void setHideBanner(SectionInfo info, boolean b) {
    getModel(info).setHideBanner(b);
  }

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);
    tree.registerInnerSection(header, id);
  }

  @Override
  public void beforeEvents(SectionInfo info) {
    HttpServletRequest request = info.getRequest();
    String params = request.getParameter("$DEBUG$"); // $NON-NLS-1$
    if (params != null) {
      info.getRootRenderContext()
          .setRenderedResponse(
              viewFactory.createResultWithModel(
                  "debug/supported.ftl", new DebugModel(info))); // $NON-NLS-1$
    }
  }

  public static class DebugModel {
    private final SectionInfo info;

    public DebugModel(SectionInfo info) {
      this.info = info;
    }

    public DocumentParamsEvent getParams() {
      DocumentParamsEvent paramsEvent = new DocumentParamsEvent();
      try {
        info.processEvent(paramsEvent);
      } catch (Exception e) {
        // log and ignore. All manner of weirdness may be thrown. That's
        // why we're debugging!
        String errMsg = "DebugModel threw, message: " + e.getLocalizedMessage();
        StackTraceElement[] stacktrace = e.getStackTrace();
        if (stacktrace != null && stacktrace.length > 0) {
          StringBuffer sb = new StringBuffer();
          for (int i = 0; i < 4 && i < stacktrace.length; ++i) {
            sb.append("\n\tat ");
            sb.append(stacktrace[i].toString());
          }
          sb.append("\n\t....");
          errMsg += sb.toString();
        }
        LOGGER.warn(errMsg);
      }
      return paramsEvent;
    }

    public List<SectionTree> getTrees() {
      MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
      return minfo.getTrees();
    }
  }
}
