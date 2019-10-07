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

import com.tle.beans.user.UserInfoBackup;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.CoreStrings;
import com.tle.core.services.user.UserService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.apache.log4j.Logger;

@TreeIndexed
@Bind
@SuppressWarnings("nls")
public class UserLinkSection extends AbstractPrototypeSection<UserLinkSection.Model> {
  private static final Logger LOGGER = Logger.getLogger(UserLinkSection.class);

  @PlugKey("userlink.unknownUser")
  private static Label UNKNOWN_USER_LABEL;

  @PlugKey("rolelink.role")
  private static String KEY_ROLETEXT;

  @PlugKey("rolelink.unknown")
  private static String KEY_UNKNOWNROLE;

  @PlugKey("userlink.systemuser")
  private static Label LABEL_SYSTEMUSER;

  @PlugKey("userlabel.lastownersuffix")
  private static Label LAST_OWNER_SUFFIX;

  private static String KEY_IMPERSONATEDBY = "userlink.impersonatedby";

  @EventFactory private EventGenerator events;

  @Inject private UserService userService;

  private SubmitValuesFunction userClickedFunc;

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);

    userClickedFunc = events.getSubmitValuesFunction("userClicked");
  }

  @EventHandlerMethod
  public void userClicked(SectionInfo info, String userId) {
    // TODO
    throw new RuntimeException("TODO");
  }

  public List<HtmlLinkState> createLinks(SectionInfo info, Collection<String> userIds) {
    List<HtmlLinkState> rv = new ArrayList<HtmlLinkState>(userIds.size());
    for (String userId : userIds) {
      rv.add(createLink(info, userId));
    }
    return rv;
  }

  public List<HtmlLinkState> createLinksFromBeans(SectionInfo info, Collection<UserBean> users) {
    List<HtmlLinkState> rv = new ArrayList<HtmlLinkState>(users.size());
    for (UserBean user : users) {
      rv.add(createLinkFromBean(info, user));
    }

    return rv;
  }

  public List<HtmlLinkState> createRoleLinks(SectionInfo info, Collection<String> userIds) {
    List<HtmlLinkState> rv = new ArrayList<HtmlLinkState>(userIds.size());
    for (String userId : userIds) {
      rv.add(createRoleLink(info, userId));
    }
    return rv;
  }

  public HtmlLinkState createRoleLink(SectionInfo info, String roleId) {
    Model model = getModel(info);
    if (!model.foundRoles.containsKey(roleId)) {
      model.rolesToFind.add(roleId);
    }
    return new RoleLinkState(roleId, info);
  }

  public HtmlLinkState createLink(SectionInfo info, String userId) {
    return createLink(info, userId, null, false);
  }

  public HtmlLinkState createLink(
      SectionInfo info, String userId, String impersonatedBy, boolean useLastOwnerDetails) {
    Model model = getModel(info);
    if ("system".equals(userId)) {
      HtmlLinkState userLink = new HtmlLinkState(LABEL_SYSTEMUSER);
      userLink.setDisabled(true);
      return userLink;
    }
    if (!model.foundUsers.containsKey(userId)) {
      model.usersToFind.add(userId);
    }
    return new UserLinkState(userId, info, impersonatedBy, useLastOwnerDetails);
  }

  public Label createLabel(SectionInfo info, String userId) {
    Model model = getModel(info);
    if ("system".equals(userId)) {
      return LABEL_SYSTEMUSER;
    }
    if (!model.foundUsers.containsKey(userId)) {
      model.usersToFind.add(userId);
    }
    return new UserLabel(userId, info);
  }

  public HtmlLinkState createLinkFromBean(SectionInfo info, UserBean user) {
    Model model = getModel(info);
    final String userId = user.getUniqueID();
    model.foundUsers.put(userId, user);
    return createLink(info, userId);
  }

  private UserBean ensureUserLookup(SectionInfo info, String userId) {
    return ensureUserLookup(info, userId, false);
  }

  private UserBean ensureUserLookup(SectionInfo info, String userId, boolean useLastOwnerDetails) {
    try {
      Model model = getModel(info);
      Map<String, UserBean> foundUsers = model.foundUsers;
      Set<String> usersToFind = model.usersToFind;
      if (usersToFind.contains(userId)) {
        Map<String, UserBean> found = userService.getInformationForUsers(usersToFind);
        foundUsers.putAll(found);

        // Set any unfound users to null
        usersToFind.removeAll(found.keySet());
        for (String utf : usersToFind) {
          foundUsers.put(utf, null);
        }
        usersToFind.clear();
      }
      UserBean userDetails = foundUsers.get(userId);
      if (userDetails == null && useLastOwnerDetails) {
        userDetails = userService.findUserInfoBackup(userId);
      }
      return userDetails;
    } catch (Exception t) {
      LOGGER.error("Error getting user details for display", t);
      return null;
    }
  }

  public RoleBean ensureRoleLookup(SectionInfo info, String roleId) {
    Model model = getModel(info);
    Map<String, RoleBean> foundRoles = model.foundRoles;
    Set<String> rolesToFind = model.rolesToFind;
    if (rolesToFind.contains(roleId)) {
      Map<String, RoleBean> found = userService.getInformationForRoles(rolesToFind);
      foundRoles.putAll(found);

      // Set any unfound roles to null
      rolesToFind.removeAll(found.keySet());
      for (String rtf : rolesToFind) {
        foundRoles.put(rtf, null);
      }
      rolesToFind.clear();
    }
    return foundRoles.get(roleId);
  }

  public static class Model {
    Set<String> usersToFind = new HashSet<String>();
    Set<String> rolesToFind = new HashSet<String>();
    Map<String, UserBean> foundUsers = new HashMap<String, UserBean>();
    Map<String, RoleBean> foundRoles = new HashMap<String, RoleBean>();
  }

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new Model();
  }

  public class UserLabel implements Label {
    private final SectionInfo info;
    private final String userId;
    private boolean lookedUp;
    private String text;

    public UserLabel(String userId, SectionInfo info) {
      this.userId = userId;
      this.info = info;
    }

    private void ensureLookup() {
      if (!lookedUp) {
        UserBean userBean = ensureUserLookup(info, userId);
        if (userBean == null) {
          text = UNKNOWN_USER_LABEL.getText();
        } else {
          // TODO: oh dear, not i18n friendly :)
          text = userBean.getFirstName() + " " + userBean.getLastName();
        }
        lookedUp = true;
      }
    }

    @Override
    public String getText() {
      ensureLookup();
      return text;
    }

    @Override
    public boolean isHtml() {
      return false;
    }
  }

  public class UserLinkState extends HtmlLinkState {
    private final SectionInfo info;
    private final String userId;
    private final String impersonatedBy;
    private boolean lookedUp;
    private boolean useLastOwnerDetails;

    public UserLinkState(
        String userId, SectionInfo info, String impersonatedBy, boolean useLastOwnerDetails) {
      this.userId = userId;
      this.info = info;
      this.impersonatedBy = impersonatedBy;
      this.useLastOwnerDetails = useLastOwnerDetails;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<HtmlLinkState> getClassForRendering() {
      return HtmlLinkState.class;
    }

    @Override
    public Label getLabel() {
      ensureLookup(useLastOwnerDetails);
      return super.getLabel();
    }

    @Override
    public Label getTitle() {
      ensureLookup(useLastOwnerDetails);
      return super.getTitle();
    }

    @Override
    public boolean isDisabled() {
      // TODO: remove this to enable links once the "userClicked" method
      // above
      // is implemented and a page exists to actually go to.
      return true;
    }

    private void ensureLookup(boolean useLastOwnerDetails) {
      if (!lookedUp) {
        UserBean userBean = ensureUserLookup(info, userId, useLastOwnerDetails);
        if (userBean == null) {
          setLabel(UNKNOWN_USER_LABEL);
          setTitle(new TextLabel(userId));
        } else {
          String userFullName = userBean.getFirstName() + " " + userBean.getLastName();
          if (userBean instanceof UserInfoBackup) {
            userFullName = userFullName + LAST_OWNER_SUFFIX.getText();
          }
          TextLabel userFullNameLabel = new TextLabel(userFullName);
          Label impersonateLabel =
              impersonatedBy == null
                  ? null
                  : new KeyLabel(
                      CoreStrings.lookup().key(KEY_IMPERSONATEDBY),
                      new TextLabel(impersonatedBy),
                      userFullNameLabel);
          setLabel(impersonateLabel != null ? impersonateLabel : userFullNameLabel);
          setTitle(new TextLabel(userBean.getUsername()));
          setClickHandler(new OverrideHandler(userClickedFunc, userBean.getUniqueID()));
        }
        lookedUp = true;
      }
    }
  }

  public class RoleLinkState extends HtmlLinkState {
    private final SectionInfo info;
    private final String roleId;
    private boolean lookedUp;

    public RoleLinkState(String roleId, SectionInfo info) {
      this.roleId = roleId;
      this.info = info;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<HtmlLinkState> getClassForRendering() {
      return HtmlLinkState.class;
    }

    @Override
    public Label getLabel() {
      ensureLookup();
      return super.getLabel();
    }

    @Override
    public Label getTitle() {
      ensureLookup();
      return super.getTitle();
    }

    @Override
    public boolean isDisabled() {
      return true;
    }

    private void ensureLookup() {
      if (!lookedUp) {
        RoleBean roleBean = ensureRoleLookup(info, roleId);
        if (roleBean != null) {
          setLabel(new KeyLabel(KEY_ROLETEXT, roleBean.getName()));
        } else {
          setLabel(new KeyLabel(KEY_UNKNOWNROLE, roleId));
        }
        lookedUp = true;
      }
    }
  }
}
