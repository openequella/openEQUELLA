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

package com.tle.core.hierarchy.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.hierarchy.HierarchyTreeNode;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.SecurityTarget;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.TargetId;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyService;
import com.tle.core.security.PrivilegeTreeProvider;

@Bind
@Singleton
public class HierarchyPrivilegeTreeProvider implements PrivilegeTreeProvider
{
	@Inject
	private HierarchyService hierarchyService;

	@Override
	public void mapTargetIdsToNames(Collection<TargetId> targetIds, Map<TargetId, String> results)
	{
		for( TargetId targetId : targetIds )
		{
			String target = targetId.getTarget();
			if( target.startsWith(SecurityConstants.TARGET_HIERARCHY_TOPIC) )
			{
				long id = Long.parseLong(target.substring(2));
				results.put(targetId, CurrentLocale.get(hierarchyService.getHierarchyTopicName(id)));
			}
		}
	}

	@Override
	public void gatherChildTargets(List<SecurityTarget> childTargets, SecurityTarget target)
	{
		if( target == null )
		{
			childTargets.add(new SecurityTarget(CurrentLocale.get("com.tle.web.hierarchy.privilegetree.rootname"),
				Node.HIERARCHY_TOPIC, null, true));
		}
		else if( target.getTargetType() == Node.HIERARCHY_TOPIC )
		{
			HierarchyTreeNode parent = (HierarchyTreeNode) target.getTarget();
			List<HierarchyTreeNode> nodes = hierarchyService.listTreeNodes(parent == null ? -1 : parent.getId());
			for( HierarchyTreeNode htn : nodes )
			{
				childTargets.add(new SecurityTarget(htn.getName(), Node.HIERARCHY_TOPIC, htn, true));
			}
		}
	}
}
