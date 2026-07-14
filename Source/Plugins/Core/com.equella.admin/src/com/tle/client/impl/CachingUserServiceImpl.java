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

package com.tle.client.impl;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.core.remoting.RemoteUserService;
import java.util.List;

public class CachingUserServiceImpl implements RemoteUserService {
  private final RemoteUserService remoteUserService;

  public CachingUserServiceImpl(RemoteUserService remoteUserService) {
    this.remoteUserService = remoteUserService;
  }

  @Override
  public void keepAlive() {
    remoteUserService.keepAlive();
  }

  @Override
  public List<String> getTokenSecretIds() {
    return remoteUserService.getTokenSecretIds();
  }

  @Override
  public UserManagementSettings getPluginConfig(String settingsConfig) {
    return remoteUserService.getPluginConfig(settingsConfig);
  }

  // TODO: OEQ-2931 remove me.
  @Override
  public UserManagementSettings getReadOnlyPluginConfig(String settingsConfig) {
    return remoteUserService.getReadOnlyPluginConfig(settingsConfig);
  }

  @Override
  public void setPluginConfig(UserManagementSettings config) {
    remoteUserService.setPluginConfig(config);
  }
}
