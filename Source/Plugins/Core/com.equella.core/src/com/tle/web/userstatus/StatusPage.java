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

package com.tle.web.userstatus;

import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.template.Decorations;
import java.util.Collection;
import javax.inject.Inject;

@Bind
@SuppressWarnings("nls")
public class StatusPage extends AbstractPrototypeSection<StatusPage.StatusModel>
    implements HtmlRenderer {
  @Inject private UserService userService;

  @ViewFactory private FreemarkerFactory viewFactory;

  public static class StatusModel {
    private UserState userState;
    private Collection<GroupBean> groups;
    private Collection<RoleBean> roles;

    public UserState getUserState() {
      return userState;
    }

    public void setUserState(UserState userState) {
      this.userState = userState;
    }

    public Collection<GroupBean> getGroups() {
      return groups;
    }

    public void setGroups(Collection<GroupBean> groups) {
      this.groups = groups;
    }

    public Collection<RoleBean> getRoles() {
      return roles;
    }

    public void setRoles(Collection<RoleBean> roles) {
      this.roles = roles;
    }
  }

  @Override
  public Class<StatusModel> getModelClass() {
    return StatusModel.class;
  }

  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    Decorations.getDecorations(context).setTitle(new TextLabel("User Status"));

    StatusModel model = getModel(context);
    UserState state = CurrentUser.getUserState();
    model.setUserState(state);
    model.setGroups(userService.getInformationForGroups(state.getUsersGroups()).values());
    model.setRoles(userService.getInformationForRoles(state.getUsersRoles()).values());
    return viewFactory.createResult("debug/statuspage.ftl", context);
  }

  @Override
  public String getDefaultPropertyName() {
    return "";
  }

  /**
   * Remove when this is spring 2.5
   *
   * @param userService
   */
  public void setUserService(UserService userService) {
    this.userService = userService;
  }
}
