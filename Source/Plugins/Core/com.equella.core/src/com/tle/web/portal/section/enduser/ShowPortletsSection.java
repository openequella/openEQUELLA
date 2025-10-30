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

package com.tle.web.portal.section.enduser;

import static com.tle.web.portal.service.PortletWebServiceImpl.UUID_SECTIONID_MAP_KEY;

import com.tle.common.portal.entity.Portlet;
import com.tle.common.portal.entity.PortletPreference;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.portal.service.PortletService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.portal.renderer.PortletRendererWrapper;
import com.tle.web.portal.service.PortletWebService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.layout.CombinedLayout;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.events.AfterParametersListener;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.events.RenderEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.StandardRenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.jquery.libraries.JQuerySortable;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowPortletsSection
    extends AbstractPrototypeSection<ShowPortletsSection.ShowPortletsModel>
    implements HtmlRenderer, ParametersEventListener, AfterParametersListener {
  protected static final PluginResourceHelper resources =
      ResourcesService.getResourceHelper(RootPortletSection.class);

  private static final ExternallyDefinedFunction SETUP_PORTAL =
      new ExternallyDefinedFunction(
          "setupPortal",
          JQuerySortable.PRERENDER, // $NON-NLS-1$
          new IncludeFile(resources.url("scripts/portal.js"))); // $NON-NLS-1$

  private static final CssInclude CSS =
      CssInclude.include(resources.url("css/portal.css")).make(); // $NON-NLS-1$

  private JSCallable portletMovedFunction;

  @Inject private PortletService portletService;
  @Inject private PortletWebService portletWebService;

  @ViewFactory private FreemarkerFactory view;
  @AjaxFactory private AjaxGenerator ajax;
  @EventFactory private EventGenerator events;

  private static final Logger LOGGER = LoggerFactory.getLogger(ShowPortletsSection.class);

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new ShowPortletsModel(info);
  }

  @Override
  @SuppressWarnings("nls")
  public SectionResult renderHtml(RenderEventContext context) {
    final ShowPortletsModel model = getModel(context);
    final SectionTree tree = model.getTree(false);

    // Redmine #4496 - Allow TLE_ADMINISTRATOR to fix portal noobage by not
    // having any portals.
    boolean systermUser = CurrentUser.getUserState().isSystem();
    if (!systermUser && portletWebService.hasPortlets(context, tree)) {
      context.preRender(CSS);

      if (!CurrentUser.wasAutoLoggedIn() && !CurrentUser.isGuest()) {
        JQueryCore.appendReady(
            context,
            new FunctionCallStatement(
                SETUP_PORTAL,
                new ObjectExpression(),
                new ObjectExpression("movedCallback", portletMovedFunction)));
      }

      final GenericTemplateResult res = new GenericTemplateResult();
      res.addNamedResult(
          CombinedLayout.TOP,
          portletWebService.renderPortlets(context, tree, PortletPreference.POSITION_TOP));
      res.addNamedResult(
          CombinedLayout.LEFT,
          portletWebService.renderPortlets(context, tree, PortletPreference.POSITION_LEFT));
      res.addNamedResult(
          CombinedLayout.RIGHT,
          portletWebService.renderPortlets(context, tree, PortletPreference.POSITION_RIGHT));
      return res;
    }

    model.setCreatePrivs(portletWebService.canCreate());
    ContentLayout.setLayout(context, ContentLayout.ONE_COLUMN);

    // system user has its own first page, neither active portlets nor the
    // default 'Welcome' page
    if (systermUser) {
      return view.createResult("enduser/sysnoportlets.ftl", this);
    }

    // The standard result for non-system users, when there are no active
    // portlets configured
    return view.createResult("enduser/noportlets.ftl", this);
  }

  @Override
  public void afterParameters(SectionInfo info, ParametersEvent event) {
    getModel(info).getTree(true);
  }

  /**
   * Event handler that should ONLY be used together with `LegacyContentApi` to retrieve the content
   * of a Legacy portlet in raw HTML format for the New UI Dashboard.
   */
  @EventHandlerMethod
  public void getPortletContent(SectionInfo info, String uuid) {
    SectionTree tree = getModel(info).getTree(false);
    // This is the mapping between Portlet UUID and Section 'PortletRendererWrapper'
    Map<String, PortletRendererWrapper> portletMapping = tree.getAttribute(UUID_SECTIONID_MAP_KEY);

    Optional.ofNullable(portletMapping.get(uuid))
        .map(PortletRendererWrapper::getDelegate)
        .ifPresentOrElse(
            portletRenderer -> renderPortletForNewDashboard(info, portletRenderer),
            () ->
                LOGGER.warn(
                    "Failed to find Legacy Section 'PortletRendererWrapper' with UUID {}", uuid));
  }

  @AjaxMethod
  public boolean portletMoved(SectionInfo info, int position, String prevUuid, String portletUuid) {
    if (CurrentUser.wasAutoLoggedIn()) {
      return false;
    }

    final Portlet prevPortlet = portletService.getByUuid(prevUuid);
    final Portlet portlet = portletService.getByUuid(portletUuid);

    portletWebService.move(info, prevPortlet, portlet, position);
    return true;
  }

  @Override
  public Class<ShowPortletsModel> getModelClass() {
    return ShowPortletsModel.class;
  }

  @Override
  public String getDefaultPropertyName() {
    return "psh"; //$NON-NLS-1$
  }

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);
    portletMovedFunction = ajax.getAjaxFunction("portletMoved"); // $NON-NLS-1$
  }

  /** This is for debugging / development purposes ONLY! */
  @Override
  @SuppressWarnings("nls")
  public void handleParameters(SectionInfo info, ParametersEvent event) throws Exception {
    String p = event.getParameter("cmd", false);
    if (p != null && p.equals("cleartree")) {
      portletWebService.clearPortletRendererCache(CurrentUser.getUserID());
    }
  }

  private void renderPortletForNewDashboard(SectionInfo info, PortletContentRenderer<?> renderer) {
    StandardRenderContext renderContext = info.getAttributeForClass(StandardRenderContext.class);
    renderer.setupForNewDashboard(renderContext);

    RenderEvent event =
        new RenderEvent(
            renderContext,
            renderer,
            (result, id) -> info.getRootRenderContext().setRenderedBody(result));
    renderContext.processEvent(event);
  }

  public class ShowPortletsModel {
    private SectionInfo info;

    public ShowPortletsModel(SectionInfo info) {
      this.info = info;
    }

    private boolean createPrivs;
    private SectionTree lastTree;

    public SectionTree getTree(boolean processParams) {
      final SectionTree portalTree = portletWebService.getPortletRendererTree(info);

      if (lastTree == portalTree) {
        return lastTree;
      }
      MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
      if (lastTree != null) {
        minfo.removeTree(lastTree);
      }
      minfo.addTreeToBottom(portalTree, processParams);
      lastTree = portalTree;
      return portalTree;
    }

    public boolean isCreatePrivs() {
      return createPrivs;
    }

    public void setCreatePrivs(boolean noCreatePrivs) {
      this.createPrivs = noCreatePrivs;
    }
  }
}
