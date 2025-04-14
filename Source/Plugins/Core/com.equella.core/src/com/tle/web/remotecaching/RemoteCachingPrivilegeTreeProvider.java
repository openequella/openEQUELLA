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

package com.tle.web.remotecaching;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
public class RemoteCachingPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider {
  /**
   * The string id expression to SettingsTarget constructor is exactly as spelled in the old
   * invocation to construct a SystemSettings in SettingsPrivilegeTreeProvider::gatherChildTargets.
   * Accordingly inconsistency with regard to camel-case/lower case etc across different sub-classes
   * of AbstractSettingsPrivilegeTreeProvider is a legacy
   */
  public RemoteCachingPrivilegeTreeProvider() {
    super(
        Type.SYSTEM_SETTING,
        ResourcesService.getResourceHelper(RemoteCachingPrivilegeTreeProvider.class)
            .key("securitytree.remotecaching"),
        new SettingsTarget("cache"));
  }
}
