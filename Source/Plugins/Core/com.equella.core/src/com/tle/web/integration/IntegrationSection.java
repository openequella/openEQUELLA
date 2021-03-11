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

package com.tle.web.integration;

import com.tle.web.integration.service.IntegrationService;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.equella.ModalSession;
import com.tle.web.sections.equella.layout.InnerLayout;
import com.tle.web.sections.events.BeforeEventsListener;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.ForwardEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.SelectionsMadeCallback;
import java.util.Collection;
import java.util.Set;
import javax.inject.Inject;

@SuppressWarnings("nls")
public class IntegrationSection
    extends AbstractPrototypeSection<IntegrationSection.IntegrationModel>
    implements ForwardEventListener, HtmlRenderer, SelectionsMadeCallback, BeforeEventsListener {
  private static final long serialVersionUID = 1L;

  @Inject private IntegrationService integrationService;

  @Override
  public boolean executeSelectionsMade(SectionInfo info, SelectionSession session) {
    Collection<SelectedResource> selectedResources = session.getSelectedResources();
    if (selectedResources.isEmpty()) {
      executeModalFinished(info, session);
    } else {
      IntegrationInterface integration = integrationService.getIntegrationInterface(info);
      integrationService.logSelections(info, session);
      return integration.select(info, session);
    }

    return false;
  }

  @Override
  public void executeModalFinished(SectionInfo info, ModalSession session) {
    IntegrationInterface integration = integrationService.getIntegrationInterface(info);
    info.forwardToUrl(integration.getClose());
  }

  @Override
  public String getDefaultPropertyName() {
    return "_int";
  }

  @Override
  public Class<IntegrationModel> getModelClass() {
    return IntegrationModel.class;
  }

  public static class IntegrationModel {
    @Bookmarked(contexts = BookmarkEvent.CONTEXT_SESSION)
    private String id;

    // This field is primarily used to let the new Search UI filter search results by MIME types
    // provided by Integrations.
    // The approach is to add these MIME types to the URL as query strings and then convert
    // Legacy Search params to SearchOptions.
    // For example, a URL would be 'searching.do?_sl.stateId=1&_int.id_2&_int.mimeTypes=xxx'.
    // To achieve this, we must use annotation 'Bookmarked'. For details, please look
    // 'BookmarkEvent', 'AnnotatedBookmarkScanner',
    // 'AnnotationBookmarkListener' and 'InfoBookmark - getBookmarkParams'.
    @Bookmarked(contexts = BookmarkEvent.CONTEXT_SESSION)
    private Set<String> mimeTypes;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public Set<String> getMimeTypes() {
      return mimeTypes;
    }

    public void setMimeTypes(Set<String> mimeTypes) {
      this.mimeTypes = mimeTypes;
    }
  }

  public String getStateId(SectionInfo info) {
    IntegrationModel model = getModel(info);
    return model.getId();
  }

  public void newSession(SectionInfo info, String id) {
    IntegrationModel model = getModel(info);
    model.setId(id);
  }

  public void updateModelMimeTypes(SectionInfo info, Set<String> mimeTypes) {
    IntegrationModel model = getModel(info);
    model.setMimeTypes(mimeTypes);
  }

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);
    tree.setAttribute(IntegrationService.KEY_INTEGRATION_CALLBACK, this);
  }

  @Override
  public void forwardCreated(SectionInfo info, SectionInfo forward) {
    newSession(forward, getStateId(info));
  }

  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    return renderToTemplate(context, context.getWrappedRootId(context));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void beforeEvents(SectionInfo info) {
    MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
    SectionTree tree = getTree();
    IntegrationSessionData data = integrationService.getSessionData(info);
    if (data == null) {
      minfo.removeTree(tree);
    } else {
      minfo.queueTreeEvents(tree);
      Integration<IntegrationSessionData> integrationServiceForData =
          (Integration<IntegrationSessionData>)
              integrationService.getIntegrationServiceForData(data);
      IntegrationImpl integration = new IntegrationImpl(data, integrationServiceForData);
      info.setAttribute(IntegrationInterface.class, integration);
      InnerLayout.addLayoutSelector(info, integration.createLayoutSelector(info));

      if (data.isForSelection()) {
        integrationService.checkIntegrationAllowed();
      }
    }
  }
}
