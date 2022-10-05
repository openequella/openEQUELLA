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

package com.tle.web.myresources;

import com.tle.mycontent.web.model.MyContentContributeModel;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.myresources.RootMyResourcesSection.RootMyResourcesModel;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.SelectionSession;
import com.tle.web.template.RenderNewSearchPage;
import com.tle.web.template.RenderNewTemplate;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;
import java.util.Map;
import org.apache.http.client.utils.URIBuilder;

public class RootMyResourcesSection extends ContextableSearchSection<RootMyResourcesModel>
    implements BlueBarEventListener {
  private static final String CONTEXT_KEY = "myResourcesContext";
  public static final String URL = "/access/myresources.do";

  @ViewFactory private FreemarkerFactory view;

  @PlugKey("myresources.menu")
  private static Label title;

  @TreeLookup private MyResourcesSearchTypeSection searchTypeSection;

  @Override
  protected String getSessionKey() {
    return CONTEXT_KEY;
  }

  @Override
  public Label getTitle(SectionInfo info) {
    return title;
  }

  /**
   * Build a URL to access a specific view of My resources in Old UI.
   *
   * @param resourceType Resource type used to indicate which view of My resources to be displayed.
   */
  public static String buildForwardUrl(String resourceType, Map<String, String> queryParams) {
    URIBuilder builder = new URIBuilder();
    builder.setPath(URL);
    builder.setParameter("type", resourceType);

    queryParams.forEach(builder::setParameter);

    return builder.toString();
  }

  public static SectionInfo createForward(SectionInfo from) {
    return from.createForward(URL);
  }

  @Override
  protected ContentLayout getDefaultLayout(SectionInfo info) {
    return selectionService.getCurrentSession(info) != null
        ? super.getDefaultLayout(info)
        : ContentLayout.ONE_COLUMN;
  }

  @Override
  public void addBlueBarResults(RenderContext context, BlueBarEvent event) {
    event.addHelp(view.createResult("mainhelp.ftl", this)); // $NON-NLS-1$
  }

  @Override
  protected boolean hasContextBeenSpecified(SectionInfo info) {
    return getModel(info).isUpdateContext();
  }

  @Override
  protected String getPageName() {
    return URL;
  }

  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    SelectionSession selectionSession = selectionService.getCurrentSession(context);
    if (selectionSession != null && RenderNewTemplate.isNewUIEnabled()) {
      getModel(context).setNewUIContent(RenderNewSearchPage.renderNewMyResourcesPage(context));
    }
    return super.renderHtml(context);
  }

  @Override
  public Class<RootMyResourcesModel> getModelClass() {
    return RootMyResourcesModel.class;
  }

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new RootMyResourcesModel();
  }

  // The reason for adding this new model class is we want to add 'newUIStateId' to
  // '/access/myresources.do' as a query string to
  // help My resources new UI return to the correct state.
  public static class RootMyResourcesModel extends ContextableSearchSection.Model {

    /**
     * In Selection Session where My resources New UI is displayed in Old UI, use this field to save
     * the New UI state ID. For the one used in full New UI mode, check {@link
     * MyContentContributeModel#newUIStateId}
     */
    @Bookmarked(contexts = BookmarkEvent.CONTEXT_SESSION, parameter = "newUIStateId")
    private String newUIStateId;

    public String getNewUIStateId() {
      return newUIStateId;
    }

    public void setNewUIStateId(String newUIStateId) {
      this.newUIStateId = newUIStateId;
    }
  }
}
