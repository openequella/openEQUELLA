/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.cloudprovider;

import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.SecurityTarget;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.TargetId;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.CoreStrings;
import com.tle.core.security.PrivilegeTreeProvider;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Bind
public class CloudProviderSettingPrivilegeTreeProvider implements PrivilegeTreeProvider {

  @Override
  public void mapTargetIdsToNames(Collection<TargetId> targetIds, Map<TargetId, String> results) {}

  @Override
  public void gatherChildTargets(List<SecurityTarget> childTargets, SecurityTarget target) {
    if (target == null) {
      childTargets.add(
          new SecurityTarget(
              CoreStrings.text("securitytree.cloudprovider"), Node.ALL_CLOUD_PROVIDER, null, true));
    }
  }
}
