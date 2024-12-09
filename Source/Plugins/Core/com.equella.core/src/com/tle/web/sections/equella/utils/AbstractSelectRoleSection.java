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

package com.tle.web.sections.equella.utils;

import com.google.common.base.Strings;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.core.services.user.UserService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.libraries.JQueryTextFieldHint;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.Option;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

@SuppressWarnings("nls")
public abstract class AbstractSelectRoleSection<M extends AbstractSelectRoleSection.Model>
    extends AbstractPrototypeSection<M> implements HtmlRenderer {
  static {
    PluginResourceHandler.init(AbstractSelectRoleSection.class);
  }

  public static final String RESULTS_DIVID = "results";

  protected static final int SEARCH_LIMIT = 50;

  @PlugKey("utils.selectroledialog.validation.enterquery")
  private static Label ENTER_QUERY_LABEL;

  @PlugKey("utils.selectroledialog.default.prompt")
  private static Label LABEL_DEFAULT_PROMPT;

  @PlugKey("utils.selectroledialog.validation.invalid")
  private static String KEY_INVALID_QUERY;

  @PlugKey("utils.selectroledialog.textfieldhint")
  private static Label TEXTFIELD_HINT;

  // The following are not static as they can be changed by setters
  private Label prompt = LABEL_DEFAULT_PROMPT;
  private Label title = SelectRoleDialog.getTitleLabel();

  @ViewFactory protected FreemarkerFactory viewFactory;
  @EventFactory protected EventGenerator events;
  @AjaxFactory protected AjaxGenerator ajax;

  @Component(name = "q")
  protected TextField query;

  @Component(name = "s")
  @PlugKey("utils.selectroledialog.searchbutton")
  private Button search;

  @Component(name = "t")
  private Link roleFilterTooltip;

  // Dynamic @Component
  private MultiSelectionList<RoleBean> roleList;

  @Inject protected ComponentFactory componentFactory;
  @Inject protected UserService userService;

  protected boolean multipleRoles;

  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    doValidationMessages(context);

    return viewFactory.createResult("utils/selectrole.ftl", this);
  }

  protected void doValidationMessages(SectionInfo info) {
    final M model = getModel(info);
    final String theQuery = query.getValue(info);
    if (!Check.isEmpty(theQuery) && !validQuery(theQuery)) {
      model.setInvalidMessageKey(KEY_INVALID_QUERY);
    } else {
      model.setInvalidMessageKey(null);
    }
    model.setHasNoResults(isNoResults(info));
  }

  protected boolean isNoResults(SectionInfo info) {
    return (!Check.isEmpty(query.getValue(info))
        && roleList.getListModel().getOptions(info).size() == 0);
  }

  protected boolean validQuery(String query) {
    String q = Strings.nullToEmpty(query);
    for (int i = 0; i < q.length(); i++) {
      if (Character.isLetterOrDigit(q.codePointAt(i))) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);

    if (multipleRoles) {
      roleList =
          (MultiSelectionList<RoleBean>) componentFactory.createMultiSelectionList(id, "ul", tree);
    } else {
      roleList =
          (MultiSelectionList<RoleBean>) componentFactory.createSingleSelectionList(id, "ul", tree);
    }

    final RoleSearchModel listModel =
        new RoleSearchModel(query, userService, SEARCH_LIMIT) {
          @Override
          protected Option<RoleBean> convertToOption(SectionInfo info, RoleBean role) {
            return new SelectRoleResultOption(role);
          }
        };

    roleList.setListModel(listModel);

    query.addTagProcessor(new JQueryTextFieldHint(TEXTFIELD_HINT, query));
  }

  protected abstract JSCallable getResultUpdater(SectionTree tree, ParameterizedEvent eventHandler);

  @Override
  public void treeFinished(String id, SectionTree tree) {
    super.treeFinished(id, tree);

    OverrideHandler handler = new OverrideHandler(getResultUpdater(tree, null));
    handler.addValidator(
        Js.validator(Js.notEquals(query.createGetExpression(), Js.str("")))
            .setFailureStatements(Js.alert_s(ENTER_QUERY_LABEL)));
    search.setClickHandler(handler);
  }

  @EventHandlerMethod
  public void addRoles(SectionInfo info) {
    final List<RoleBean> roles = roleList.getSelectedValues(info);
    if (!multipleRoles && roles.size() > 0) {
      setSelections(info, null);
    }

    for (RoleBean role : roles) {
      addSelectedRole(info, role.getUniqueID(), Format.format(role));
    }
  }

  public void addSelectedRole(SectionInfo info, String uuid, String displayName) {
    final SelectedRole role = createSelectedRole(info, uuid, displayName);
    if (role != null) {
      List<SelectedRole> selections = getSelections(info);
      if (selections == null) {
        selections = new ArrayList<SelectedRole>();
      }
      selections.remove(role);
      selections.add(role);
      setSelections(info, selections);
    }
  }

  protected abstract SelectedRole createSelectedRole(
      SectionInfo info, String uuid, String displayName);

  void setSelections(SectionInfo info, List<SelectedRole> selections) {
    getModel(info).setSelections(selections);
  }

  public List<SelectedRole> getSelections(SectionInfo info) {
    return getModel(info).getSelections();
  }

  public TextField getQuery() {
    return query;
  }

  public MultiSelectionList<RoleBean> getRoleList() {
    return roleList;
  }

  public Button getSearch() {
    return search;
  }

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new Model();
  }

  public boolean isMultipleRoles() {
    return multipleRoles;
  }

  public void setMultipleRoles(boolean multipleRoles) {
    this.multipleRoles = multipleRoles;
  }

  public Link getRoleFilterTooltip() {
    return roleFilterTooltip;
  }

  public static class SelectRoleResultOption implements Option<RoleBean> {
    private final RoleBean role;
    private boolean disabled;

    protected SelectRoleResultOption(RoleBean role) {
      this(role, false);
    }

    protected SelectRoleResultOption(RoleBean role, boolean disabled) {
      this.role = role;
      this.disabled = disabled;
    }

    @Override
    public RoleBean getObject() {
      return role;
    }

    public Label getRolename() {
      return new TextLabel(role.getName());
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public String getValue() {
      return role.getUniqueID();
    }

    @Override
    public String getAltTitleAttr() {
      return null;
    }

    @Override
    public String getGroupName() {
      return null;
    }

    @Override
    public boolean isDisabled() {
      return false;
    }

    @Override
    public boolean isNameHtml() {
      return false;
    }

    @Override
    public boolean hasAltTitleAttr() {
      return false;
    }

    @Override
    public void setDisabled(boolean disabled) {
      this.disabled = disabled;
    }
  }

  public static class Model {
    @Bookmarked(name = "s")
    private List<SelectedRole> selections;

    @Bookmarked(name = "ex", ignoreForContext = BookmarkEvent.CONTEXT_BROWSERURL)
    private SectionRenderable topRenderable;

    private HtmlListState resultList;
    private boolean hasNoResults;
    private String invalidMessageKey;

    public SectionRenderable getTopRenderable() {
      return topRenderable;
    }

    public void setTopRenderable(SectionRenderable topRenderable) {
      this.topRenderable = topRenderable;
    }

    public HtmlListState getResultList() {
      return resultList;
    }

    public void setResultList(HtmlListState resultList) {
      this.resultList = resultList;
    }

    public List<SelectedRole> getSelections() {
      return selections;
    }

    public void setSelections(List<SelectedRole> selections) {
      this.selections = selections;
    }

    public boolean isHasNoResults() {
      return hasNoResults;
    }

    public void setHasNoResults(boolean hasNoResults) {
      this.hasNoResults = hasNoResults;
    }

    public String getInvalidMessageKey() {
      return invalidMessageKey;
    }

    public void setInvalidMessageKey(String invalidMessageKey) {
      this.invalidMessageKey = invalidMessageKey;
    }
  }

  public Label getPrompt() {
    return prompt;
  }

  public void setPrompt(Label prompt) {
    this.prompt = prompt;
  }

  public Label getTitle() {
    return title;
  }

  public void setTitle(Label title) {
    this.title = title;
  }
}
