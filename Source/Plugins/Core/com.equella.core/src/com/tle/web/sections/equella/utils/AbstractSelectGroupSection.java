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
public abstract class AbstractSelectGroupSection<M extends AbstractSelectGroupSection.Model>
    extends AbstractPrototypeSection<M> implements HtmlRenderer {
  static {
    PluginResourceHandler.init(AbstractSelectGroupSection.class);
  }

  public static final String RESULTS_DIVID = "results";
  public static final String SELECT_DIVID = "select-area";

  protected static final int SEARCH_LIMIT = 50;

  @PlugKey("utils.selectgroupdialog.validation.enterquery")
  private static Label ENTER_QUERY_LABEL;

  @PlugKey("utils.selectgroupdialog.default.prompt")
  private static Label LABEL_DEFAULT_PROMPT;

  @PlugKey("utils.selectgroupdialog.validation.invalid")
  private static String KEY_INVALID_QUERY;

  @PlugKey("utils.selectgroupdialog.textfieldhint")
  private static Label TEXTFIELD_HINT;

  // The following are not static as they can be changed by setters
  private Label prompt = LABEL_DEFAULT_PROMPT;
  private Label title = SelectGroupDialog.getTitleLabel();

  @ViewFactory protected FreemarkerFactory viewFactory;
  @EventFactory protected EventGenerator events;
  @AjaxFactory protected AjaxGenerator ajax;

  @Component(name = "q")
  protected TextField query;

  @Component(name = "s")
  @PlugKey("utils.selectgroupdialog.searchbutton")
  private Button search;

  @Component(name = "t")
  private Link groupFilterTooltip;

  // Dynamic @Component
  private MultiSelectionList<GroupBean> groupList;

  @Inject protected ComponentFactory componentFactory;
  @Inject protected UserService userService;

  private Set<String> groupFilter;
  private List<String> groupFilterNames; // readonly
  protected boolean multipleGroups;

  @PlugKey("utils.selectgroupdialog.link.selectallgroup")
  @Component(name = "sag")
  private Link selectAllGroupLink;

  @PlugKey("utils.selectgroupdialog.link.selectnonegroup")
  @Component(name = "sng")
  private Link selectNoneGroupLink;

  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    doValidationMessages(context);

    return viewFactory.createResult("utils/selectgroup.ftl", this);
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
    if (multipleGroups) {
      model.setShowSelectLink(groupList.getListModel().getOptions(info).size() > 0);
    }
  }

  protected boolean isNoResults(SectionInfo info) {
    return (!Check.isEmpty(query.getValue(info))
        && groupList.getListModel().getOptions(info).size() == 0);
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

    if (multipleGroups) {
      groupList =
          (MultiSelectionList<GroupBean>) componentFactory.createMultiSelectionList(id, "ul", tree);
    } else {
      groupList =
          (MultiSelectionList<GroupBean>)
              componentFactory.createSingleSelectionList(id, "ul", tree);
    }

    final GroupSearchWithExclusionsListModel listModel =
        new GroupSearchWithExclusionsListModel(query, userService, groupFilter, SEARCH_LIMIT);
    groupList.setListModel(listModel);

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

    JSCallable setAllGroupFunction = groupList.createSetAllFunction();
    selectAllGroupLink.setClickHandler(new OverrideHandler(setAllGroupFunction, true));
    selectNoneGroupLink.setClickHandler(new OverrideHandler(setAllGroupFunction, false));
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
  public void addGroups(SectionInfo info) {
    final List<GroupBean> groups = groupList.getSelectedValues(info);
    if (!multipleGroups && groups.size() > 0) {
      setSelections(info, null);
    }

    for (GroupBean group : groups) {
      addSelectedGroup(info, group.getUniqueID(), Format.format(group));
    }
  }

  public void addSelectedGroup(SectionInfo info, String uuid, String displayName) {
    final SelectedGroup group = createSelectedGroup(info, uuid, displayName);
    if (group != null) {
      List<SelectedGroup> selections = getSelections(info);
      if (selections == null) {
        selections = new ArrayList<SelectedGroup>();
      }
      selections.remove(group);
      selections.add(group);
      setSelections(info, selections);
    }
  }

  protected abstract SelectedGroup createSelectedGroup(
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

  public void setGroupExclusions(SectionInfo info, Set<String> groupExclusions) {
    getModel(info).setGroupExclusions(groupExclusions);
  }

  public List<String> getGroupFilterNames() {
    return groupFilterNames;
  }

  void setSelections(SectionInfo info, List<SelectedGroup> selections) {
    getModel(info).setSelections(selections);
  }

  public List<SelectedGroup> getSelections(SectionInfo info) {
    return getModel(info).getSelections();
  }

  public TextField getQuery() {
    return query;
  }

  public MultiSelectionList<GroupBean> getGroupList() {
    return groupList;
  }

  public Button getSearch() {
    return search;
  }

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new Model();
  }

  public boolean isMultipleGroups() {
    return multipleGroups;
  }

  public void setMultipleGroups(boolean multipleGroups) {
    this.multipleGroups = multipleGroups;
  }

  public Link getGroupFilterTooltip() {
    return groupFilterTooltip;
  }

  public Link getSelectAllGroupLink() {
    return selectAllGroupLink;
  }

  public Link getSelectNoneGroupLink() {
    return selectNoneGroupLink;
  }

  public class GroupSearchWithExclusionsListModel extends GroupSearchModel {
    public GroupSearchWithExclusionsListModel(
        TextField query, UserService userService, Set<String> groupFilter, int limit) {
      super(query, userService, groupFilter, limit);
    }

    @Override
    protected Iterable<GroupBean> populateModel(SectionInfo info) {
      final Iterable<GroupBean> groups = super.populateModel(info);
      final Set<String> groupExclusions = getModel(info).getGroupExclusions();
      if (groupExclusions != null && !groupExclusions.isEmpty()) {
        final Iterator<GroupBean> iterator = groups.iterator();
        while (iterator.hasNext()) {
          final GroupBean group = iterator.next();
          if (groupExclusions.contains(group.getUniqueID())) {
            iterator.remove();
          }
        }
      }
      return groups;
    }

    @Override
    protected Option<GroupBean> convertToOption(SectionInfo info, GroupBean group) {
      return new SelectGroupResultOption(group);
    }
  }

  public static class SelectGroupResultOption implements Option<GroupBean> {
    private final GroupBean group;
    private boolean disabled;

    protected SelectGroupResultOption(GroupBean group) {
      this(group, false);
    }

    protected SelectGroupResultOption(GroupBean group, boolean disabled) {
      this.group = group;
      this.disabled = disabled;
    }

    @Override
    public GroupBean getObject() {
      return group;
    }

    public Label getGroupname() {
      return new TextLabel(group.getName());
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public String getValue() {
      return group.getUniqueID();
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
    private List<SelectedGroup> selections;

    @Bookmarked(name = "ex", ignoreForContext = BookmarkEvent.CONTEXT_BROWSERURL)
    private Set<String> groupExclusions;

    private SectionRenderable topRenderable;
    private HtmlListState resultList;
    private boolean hasNoResults;
    private String invalidMessageKey;
    private boolean showSelectLink;

    public boolean isShowSelectLink() {
      return showSelectLink;
    }

    public void setShowSelectLink(boolean showSelectLink) {
      this.showSelectLink = showSelectLink;
    }

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

    public List<SelectedGroup> getSelections() {
      return selections;
    }

    public void setSelections(List<SelectedGroup> selections) {
      this.selections = selections;
    }

    public Set<String> getGroupExclusions() {
      return groupExclusions;
    }

    public void setGroupExclusions(Set<String> groupExclusions) {
      this.groupExclusions = groupExclusions;
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
