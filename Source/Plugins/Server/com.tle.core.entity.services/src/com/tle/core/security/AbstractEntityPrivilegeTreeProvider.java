package com.tle.core.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.SecurityTarget;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.TargetId;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.BaseEntityService;
import com.tle.web.TextBundle;
import com.tle.web.i18n.BundleCache;

public abstract class AbstractEntityPrivilegeTreeProvider<T extends BaseEntity> implements PrivilegeTreeProvider
{
	@Inject
	private BundleCache bundleCache;
	@Inject
	private BaseEntityService baseEntityService;

	private final AbstractEntityService<?, T> service;
	private final Node allNode;
	private final Node singularNode;
	private final String allNodeKey;
	private final String allTargetsKey;

	/**
	 * @param allNodeKey Key for security tree node matching all of this entity.
	 * @param allTargetsKey Key for ACL viewer target matching all of this
	 *            entity.
	 */
	protected AbstractEntityPrivilegeTreeProvider(AbstractEntityService<?, T> service, Node allNode, String allNodeKey,
		Node singularNode, String allTargetsKey)
	{
		this.service = service;
		this.allNode = allNode;
		this.allNodeKey = allNodeKey;
		this.singularNode = singularNode;
		this.allTargetsKey = allTargetsKey;
	}

	@Override
	public void mapTargetIdsToNames(Collection<TargetId> targetIds, Map<TargetId, String> results)
	{
		for( TargetId targetId : targetIds )
		{
			String target = targetId.getTarget();
			if( target.equals(SecurityConstants.TARGET_EVERYTHING) )
			{
				if( targetId.getPriority() == allNode.getOverridePriority() )
				{
					results.put(targetId, CurrentLocale.get(allTargetsKey));
				}
			}
			else if( target.startsWith(SecurityConstants.TARGET_BASEENTITY) )
			{
				long id = Long.parseLong(target.substring(2));
				results.put(targetId, CurrentLocale.get(baseEntityService.getNameForId(id)));
			}
			else
			{
				fallbackMapTargetIdToName(targetId, results);
			}
		}
	}

	@Override
	public void gatherChildTargets(List<SecurityTarget> childTargets, SecurityTarget target)
	{
		if( target == null )
		{
			childTargets.add(new SecurityTarget(CurrentLocale.get(allNodeKey), allNode, null, true));
		}
		else if( target.getTargetType() == allNode )
		{
			List<BaseEntityLabel> ts = service.listAllIncludingSystem();

			bundleCache.addBundleRefs(ts);

			for( BaseEntityLabel bel : ts )
			{
				T entity = createEntity();
				entity.setId(bel.getId());

				childTargets.add(new SecurityTarget(TextBundle.getLocalString(bel.getBundleId(), bundleCache, null,
					null), singularNode, entity, false));
			}
		}
	}

	protected void fallbackMapTargetIdToName(TargetId targetId, Map<TargetId, String> results)
	{
		// Nothing by default
	}

	protected abstract T createEntity();
}
