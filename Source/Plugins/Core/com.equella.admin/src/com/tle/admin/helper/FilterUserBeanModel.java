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

package com.tle.admin.helper;

import com.dytech.gui.filter.FilterModel;
import com.tle.admin.service.AdminUserDirectoryService;
import com.tle.common.Format;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FilterUserBeanModel extends FilterModel<UserBean> {
  private static final Log LOGGER = LogFactory.getLog(FilterUserBeanModel.class);

  private AdminUserDirectoryService userDirectoryService;

  public FilterUserBeanModel(AdminUserDirectoryService userDirectoryService) {
    this.userDirectoryService = userDirectoryService;
  }

  @Override
  public List<UserBean> search(String pattern) {
    try {
      List<UserBean> users = removeExclusions(userDirectoryService.searchUsers(pattern));
      Collections.sort(users, Format.USER_BEAN_COMPARATOR);
      return users;
    } catch (Exception ex) {
      LOGGER.warn("Error searching for users matching " + pattern, ex);
      return new ArrayList<UserBean>(0);
    }
  }
}
