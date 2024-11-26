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

import com.tle.admin.Driver;
import com.tle.admin.gui.EditorException;
import com.tle.admin.plugin.GeneralPlugin;
import com.tle.admin.service.AdminTLEUserService;
import com.tle.admin.usermanagement.internal.UsersTab;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.user.TLEUser;
import com.tle.core.remoting.RemoteTLEGroupService;
import com.tle.core.remoting.RemoteTLEUserService;
import com.tle.core.remoting.RemoteUserService;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UserWrapper extends GeneralPlugin<UserManagementSettings> {
  private UsersTab userPanel;

  public UserWrapper() {
    super();
  }

  @Override
  public void init() {
    super.init();

    userPanel =
        new UsersTab(
            new UserService(clientService.getService(RemoteTLEUserService.class)),
            clientService.getService(RemoteTLEGroupService.class),
            clientService.getService(RemoteUserService.class));
    addFillComponent(userPanel);
  }

  @Override
  public boolean hasSave() {
    return false;
  }

  @Override
  public void load(UserManagementSettings settings) {
    // Nothing to see here, move along...
  }

  @Override
  public boolean save(UserManagementSettings settings) throws EditorException {
    userPanel.save();
    return true;
  }

  @Override
  public String getDocumentName() {
    return "user";
  }

  // TODO: A temporary class to wrap the RemoteTLEUserService while the graphQL methods are
  // implemented
  //       in the AdminTLEUserService. Should probably be made central to be used anywhere the
  // RemoteTLEUserService.
  private static class UserService implements RemoteTLEUserService {
    private RemoteTLEUserService delegate;
    private AdminTLEUserService adminTLEUserService;

    private UserService() {}

    UserService(RemoteTLEUserService delegate) {
      this.delegate = delegate;
      this.adminTLEUserService =
          Optional.ofNullable(Driver.instance())
              .map(driver -> new AdminTLEUserService(driver.getClientConfiguration()))
              .orElseThrow(() -> new IllegalStateException("Driver instance not found"));
    }

    @Override
    public String add(TLEUser newUser) {
      return delegate.add(newUser);
    }

    @Override
    public String add(TLEUser newUser, boolean passwordNotHashed) {
      return delegate.add(newUser, passwordNotHashed);
    }

    @Override
    public String add(TLEUser newUser, List<String> groups) {
      return delegate.add(newUser, groups);
    }

    @Override
    public String add(String username, List<String> groups) {
      return delegate.add(username, groups);
    }

    @Override
    public TLEUser get(String id) {
      return delegate.get(id);
    }

    @Override
    public TLEUser getByUsername(String username) {
      return delegate.getByUsername(username);
    }

    @Override
    public String edit(TLEUser user, boolean passwordNotHashed) {
      return delegate.edit(user, passwordNotHashed);
    }

    @Override
    public void delete(String uuid) {
      adminTLEUserService.delete(uuid);
    }

    @Override
    public List<TLEUser> searchUsers(String query, String parentGroupID, boolean recursive) {
      return delegate.searchUsers(query, parentGroupID, recursive);
    }

    @Override
    public List<TLEUser> searchUsers(
        String query, String parentGroupID, boolean recursive, Integer limit) {
      return delegate.searchUsers(query, parentGroupID, recursive, limit);
    }

    @Override
    public List<TLEUser> searchUsers(
        String query, String parentGroupID, boolean recursive, Integer limit, Integer offset) {
      return delegate.searchUsers(query, parentGroupID, recursive, limit, offset);
    }

    @Override
    public void onSuspension(Set<String> uuids) {
      delegate.onSuspension(uuids);
    }

    @Override
    public int countUsers() {
      return delegate.countUsers();
    }

    @Override
    public int countUsers(String query, String parentGroupID, boolean recursive) {
      return delegate.countUsers(query, parentGroupID, recursive);
    }
  }
}
