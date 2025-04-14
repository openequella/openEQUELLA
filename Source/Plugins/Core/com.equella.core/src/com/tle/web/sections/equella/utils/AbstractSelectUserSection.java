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

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
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
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.JQuerySelector.Type;
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
import com.tle.web.sections.standard.js.modules.TooltipModule;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.Option;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

@SuppressWarnings("nls")
public abstract class AbstractSelectUserSection<M extends AbstractSelectUserSection.Model>
    extends AbstractPrototypeSection<M> implements HtmlRenderer {
  static {
    PluginResourceHandler.init(AbstractSelectUserSection.class);
  }

  public static final String RESULTS_DIVID = "results";

  protected static final int SEARCH_LIMIT = 50;

  @PlugKey("utils.selectuserdialog.validation.enterquery")
  private static Label ENTER_QUERY_LABEL;

  @PlugKey("utils.selectuserdialog.default.prompt")
  private static Label LABEL_DEFAULT_PROMPT;

  @PlugKey("utils.selectuserdialog.validation.invalid")
  private static String KEY_INVALID_QUERY;

  @PlugKey("utils.selectuserdialog.textfieldhint")
  private static Label TEXTFIELD_HINT;

  // The following are not static as they can be changed by setters
  private Label prompt = LABEL_DEFAULT_PROMPT;
  private Label title = SelectUserDialog.getTitleLabel();
  private Label subTitle;

  @ViewFactory protected FreemarkerFactory viewFactory;
  @EventFactory protected EventGenerator events;
  @AjaxFactory protected AjaxGenerator ajax;

  @Component(name = "q")
  protected TextField query;

  @Component(name = "s")
  @PlugKey("utils.selectuserdialog.searchbutton")
  private Button search;

  @Component(name = "t")
  private Link groupFilterTooltip;

  // Dynamic @Component
  private MultiSelectionList<UserBean> userList;

  @Inject protected ComponentFactory componentFactory;
  @Inject protected UserService userService;
  @Inject protected UserLinkService userLinkService;

  protected UserLinkSection userLinkSection;

  private Set<String> groupFilter;
  private List<String> groupFilterNames; // readonly
  protected boolean multipleUsers;

  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    doValidationMessages(context);

    return viewFactory.createResult("utils/selectuser.ftl", this);
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
        && userList.getListModel().getOptions(info).size() == 0);
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

    userLinkSection = userLinkService.register(tree, id);

    if (multipleUsers) {
      userList =
          (MultiSelectionList<UserBean>) componentFactory.createMultiSelectionList(id, "ul", tree);
    } else {
      userList =
          (MultiSelectionList<UserBean>) componentFactory.createSingleSelectionList(id, "ul", tree);
    }

    final UserSearchWithExclusionsListModel listModel =
        new UserSearchWithExclusionsListModel(query, userService, groupFilter, SEARCH_LIMIT);
    userList.setListModel(listModel);

    if (!Check.isEmpty(groupFilter)) {
      groupFilterTooltip.addReadyStatements(
          TooltipModule.getTooltipStatements(
              new JQuerySelector(Type.CLASS, "filterstooltip"),
              new JQuerySelector(Type.CLASS, "displayfilters"),
              250,
              110,
              0,
              false));
      groupFilterTooltip.setClickHandler(new OverrideHandler());
    }

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
  public void addUsers(SectionInfo info) {
    final List<UserBean> users = userList.getSelectedValues(info);
    if ((!multipleUsers && users.size() > 0) || (multipleUsers && Check.isEmpty(users))) {
      setSelections(info, null);
    }
    List<SelectedUser> selections = new ArrayList<SelectedUser>();
    if (!Check.isEmpty(getSelections(info))) {
      selections.addAll(getSelections(info));
    }
    for (UserBean user : users) {
      SelectedUser selectedUser = createSelectedUser(info, user.getUniqueID(), Format.format(user));
      if (!selections.contains(selectedUser)) {
        selections.add(selectedUser);
      }
    }
    setSelections(info, selections);
  }

  protected abstract SelectedUser createSelectedUser(
      SectionInfo info, String uuid, String displayName);

  /**
   * Must be set before registering the dialog
   *
   * @param groupFilter
   */
  public void setGroupFilter(Set<String> groupFilter) {
    if (!Check.isEmpty(groupFilter)) {
      this.groupFilter = groupFilter;

      final Collection<GroupBean> groupBeans =
          userService.getInformationForGroups(groupFilter).values();
      final Collection<String> gn =
          Collections2.transform(
              groupBeans,
              new Function<GroupBean, String>() {
                @Override
                public String apply(GroupBean group) {
                  return group.getName();
                }
              });
      groupFilterNames = Lists.newArrayList(gn);
      Collections.sort(groupFilterNames);
    }
  }

  public void setUserExclusions(SectionInfo info, Set<String> userExclusions) {
    getModel(info).setUserExclusions(userExclusions);
  }

  public List<String> getGroupFilterNames() {
    return groupFilterNames;
  }

  void setSelections(SectionInfo info, List<SelectedUser> selections) {
    getModel(info).setSelections(selections);
  }

  public List<SelectedUser> getSelections(SectionInfo info) {
    return getModel(info).getSelections();
  }

  public TextField getQuery() {
    return query;
  }

  public MultiSelectionList<UserBean> getUserList() {
    return userList;
  }

  public Button getSearch() {
    return search;
  }

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new Model();
  }

  public boolean isMultipleUsers() {
    return multipleUsers;
  }

  public void setMultipleUsers(boolean multipleUsers) {
    this.multipleUsers = multipleUsers;
  }

  public Link getGroupFilterTooltip() {
    return groupFilterTooltip;
  }

  public class UserSearchWithExclusionsListModel extends UserSearchModel {
    public UserSearchWithExclusionsListModel(
        TextField query, UserService userService, Set<String> groupFilter, int limit) {
      super(query, userService, groupFilter, limit);
    }

    @Override
    protected Iterable<UserBean> populateModel(SectionInfo info) {
      final Iterable<UserBean> users = super.populateModel(info);
      final Set<String> userExclusions = getModel(info).getUserExclusions();
      if (userExclusions != null && !userExclusions.isEmpty()) {
        final Iterator<UserBean> iterator = users.iterator();
        while (iterator.hasNext()) {
          final UserBean user = iterator.next();
          if (userExclusions.contains(user.getUniqueID())) {
            iterator.remove();
          }
        }
      }
      return users;
    }

    @Override
    protected Option<UserBean> convertToOption(SectionInfo info, UserBean user) {
      return new SelectUserResultOption(user, userLinkSection.createLinkFromBean(info, user));
    }
  }

  public static class SelectUserResultOption implements Option<UserBean> {
    private final UserBean user;
    private final HtmlLinkState link;
    private boolean disabled;

    protected SelectUserResultOption(UserBean user, HtmlLinkState link) {
      this.user = user;
      this.link = link;
    }

    @Override
    public UserBean getObject() {
      return user;
    }

    public HtmlLinkState getLink() {
      return link;
    }

    public Label getUsername() {
      return new TextLabel(user.getUsername());
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public String getValue() {
      return user.getUniqueID();
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

    public void setDisabled(boolean disabled) {
      this.disabled = disabled;
    }
  }

  public static class Model {
    @Bookmarked(name = "s")
    private List<SelectedUser> selections;

    @Bookmarked(name = "ex", ignoreForContext = BookmarkEvent.CONTEXT_BROWSERURL)
    private Set<String> userExclusions;

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

    public List<SelectedUser> getSelections() {
      return selections;
    }

    public void setSelections(List<SelectedUser> selections) {
      this.selections = selections;
    }

    public Set<String> getUserExclusions() {
      return userExclusions;
    }

    public void setUserExclusions(Set<String> userExclusions) {
      this.userExclusions = userExclusions;
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

  public Label getSubTitle() {
    return subTitle;
  }

  public void setSubTitle(Label subTitle) {
    this.subTitle = subTitle;
  }
}
