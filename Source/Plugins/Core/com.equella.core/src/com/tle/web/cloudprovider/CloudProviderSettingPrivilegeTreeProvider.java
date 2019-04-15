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
