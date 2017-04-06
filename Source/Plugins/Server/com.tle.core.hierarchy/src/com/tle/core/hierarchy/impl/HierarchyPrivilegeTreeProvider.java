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
