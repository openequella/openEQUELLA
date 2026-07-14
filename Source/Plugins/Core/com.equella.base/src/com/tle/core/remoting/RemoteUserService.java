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

package com.tle.core.remoting;

import com.tle.beans.ump.UserManagementSettings;
import java.util.List;

/**
 * This interface provides methods to retrieve information about users, groups and roles from the
 * user management system. The user management system is a plugin system, so the implementation of
 * this interface will be provided by a plugin. Typically, that is a collection of UserDirectory
 * plugins which are interacted with sequentially and the results aggregated.
 *
 * <p>This is different from the TLEUserService which is specifically focused on the internal TLE
 * Users. (And sits alongside TLEGroupService and TLERoleService.)
 */
public interface RemoteUserService {
  void keepAlive();

  List<String> getTokenSecretIds();

  UserManagementSettings getPluginConfig(String settingsConfig);

  UserManagementSettings getReadOnlyPluginConfig(String settingsConfig);

  void setPluginConfig(UserManagementSettings config);
}
