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

package com.tle.admin.usermanagement.internal;

import com.tle.admin.service.AdminTLEGroupService;
import com.tle.admin.service.AdminTLEUserService;
import com.tle.admin.service.BasicGroupDetails;
import com.tle.beans.user.TLEUser;
import com.tle.common.BulkImport;
import com.tle.common.Check;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.util.CsvReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserBulkImporter extends BulkImport<TLEUser> {
  private final Logger LOGGER = LoggerFactory.getLogger(UserBulkImporter.class);

  private final AdminTLEGroupService groupService;
  private final AdminTLEUserService userService;

  private String groupName;

  public UserBulkImporter(AdminTLEUserService userService, AdminTLEGroupService groupService) {
    this.userService = userService;
    this.groupService = groupService;
  }

  @Override
  public void add(TLEUser u) throws Exception {
    Optional<BasicGroupDetails> group =
        Optional.ofNullable(groupName).filter(Strings::isNotBlank).map(this::validateGroupName);

    // add user, preferably avoiding inadvertent recursion
    LOGGER.debug("Adding user: {}", u.getUsername());
    String uuid = userService.add(u);

    // add to group
    group.ifPresent(
        g -> {
          LOGGER.debug("Adding [{}] to group [{}]", u.getUsername(), g.getName());
          g.addUser(uuid);
          groupService.edit(g);
        });
  }

  @Override
  public TLEUser createNew() {
    return new TLEUser();
  }

  @Override
  public void edit(TLEUser t) {
    userService.edit(t);
  }

  @Override
  public TLEUser getOld(CsvReader reader) throws IOException {
    String uuid = reader.get("uuid");
    String username = reader.get("username");
    if (!Check.isEmpty(uuid)) {
      return userService.get(uuid).orElse(null);
    } else if (!Check.isEmpty(username)) {
      return userService.getByUsername(username).orElse(null);
    }
    return null;
  }

  @SuppressWarnings("nls")
  @Override
  public void update(CsvReader reader, TLEUser t, boolean create) throws IOException {
    if (create) {
      t.setUuid(reader.get("uuid"));
    }

    t.setEmailAddress(reader.get("email"));
    t.setFirstName(reader.get("firstname"));
    t.setLastName(reader.get("lastname"));
    t.setUsername(reader.get("username"));
    String password = reader.get("password");
    if (!Check.isEmpty(password)) {
      t.setPassword(password);
    } else if (create) {
      List<ValidationError> errors = new ArrayList<>();
      errors.add(
          new ValidationError(
              "password", CurrentLocale.get("tleuserservice.bulkimport.nopassword")));
      throw new InvalidDataException(errors);
    }

    groupName = reader.get("group");
  }

  private BasicGroupDetails validateGroupName(String name) {
    return groupService
        .getByName(name)
        .orElseThrow(
            () -> {
              List<ValidationError> errors = new ArrayList<>();
              errors.add(
                  new ValidationError(
                      "group", CurrentLocale.get("unserviceable.bulkimport.nogroup", name)));
              return new InvalidDataException(errors);
            });
  }
}
