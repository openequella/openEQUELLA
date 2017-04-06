package com.tle.core.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tle.common.security.remoting.RemotePrivilegeTreeService.SecurityTarget;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.TargetId;

public interface PrivilegeTreeProvider
{
	/**
	 * @param results Map of resolved target IDs to Language Bundles objects or
	 *            their long IDs - anything that BundleCache supports.
	 */
	void mapTargetIdsToNames(Collection<TargetId> targetIds, Map<TargetId, String> results);

	void gatherChildTargets(List<SecurityTarget> childTargets, SecurityTarget target);
}
