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

package com.tle.admin.usermanagement.standard;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.dytech.gui.filter.FilterModel;
import com.dytech.gui.filter.FilteredShuffleBox;
import com.tle.admin.Driver;
import com.tle.admin.plugin.GeneralPlugin;
import com.tle.beans.usermanagement.standard.wrapper.SuspendedUserWrapperSettings;
import com.tle.common.Format;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.remoting.RemoteUserService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Admin Console UI component targets to the configuration for suspended user account {@link
 * SuspendedUserWrapperSettings} and provides supports for user account suspension.
 */
public class SuspensionWrapper extends GeneralPlugin<SuspendedUserWrapperSettings> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SuspensionWrapper.class);
  private final FilteredShuffleBox<UserBean> fsb;

  @Override
  public void init() {
    super.init();
  }

  public SuspensionWrapper() {
    fsb = new FilteredShuffleBox<>(new GroupFilter());
    addFillComponent(fsb);
  }

  @Override
  public void load(SuspendedUserWrapperSettings settings) {
    RemoteUserService userService = clientService.getService(RemoteUserService.class);
    try {
      List<UserBean> users =
          new ArrayList<>(
              userService.getInformationForUsers(settings.getSuspendedUsers()).values());
      users.sort(Format.USER_BEAN_COMPARATOR);
      fsb.addToRight(users);
    } catch (RuntimeApplicationException e) {
      displayError(s("errorloading"), e);
    }
  }

  @Override
  public boolean save(SuspendedUserWrapperSettings settings) {
    Set<String> right = new HashSet<>();
    for (UserBean user : fsb.getRight()) {
      right.add(user.getUniqueID());
    }

    boolean saved = false;
    try {
      settings.setSuspendedUsers(right);
      saved = true;
    } catch (Exception e) {
      displayError(s("errorsaving"), e);
    }

    return saved;
  }

  protected class GroupFilter extends FilterModel<UserBean> {
    @Override
    public List<UserBean> search(String query) {
      try {
        List<UserBean> users = clientService.getService(RemoteUserService.class).searchUsers(query);
        users.sort(Format.USER_BEAN_COMPARATOR);
        return users;
      } catch (RuntimeApplicationException e) {
        displayError(s("errorsearching"), e);
        return Collections.emptyList();
      }
    }
  }

  protected void displayError(String s, Exception e) {
    LOGGER.error(s, e);
    Driver.displayInformation(fsb, s + " : " + e.getMessage());
  }

  private String s(String s) {
    return CurrentLocale.get("com.tle.admin.usermanagement.suspensionwrapper", s);
  }
}
