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

package com.tle.web.lti13.platforms.security;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.resources.ResourcesService;

@Bind
public class LTI13PlatformsSettingsPrivilegeTreeProvider
    extends AbstractSettingsPrivilegeTreeProvider {
  public LTI13PlatformsSettingsPrivilegeTreeProvider() {
    super(
        Type.SYSTEM_SETTING,
        ResourcesService.getResourceHelper(LTI13PlatformsSettingsPrivilegeTreeProvider.class)
            .key("securitytree.lti13platforms"),
        new SettingsTarget("lti13platforms"));
  }

  @Override
  public void checkAuthorised() {
    if (!isAuthorised()) {
      throw new AccessDeniedException(r.getString("lti13.platform.no.access"));
    }
  }
}