package com.tle.core.settings.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.SecurityTarget;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.TargetId;
import com.tle.core.guice.Bind;
import com.tle.core.security.PrivilegeTreeProvider;

@Bind
@Singleton
@SuppressWarnings("nls")
public class SettingsPrivilegeTreeProvider implements PrivilegeTreeProvider
{
	@Override
	public void mapTargetIdsToNames(Collection<TargetId> targetIds, Map<TargetId, String> results)
	{
		for( TargetId targetId : targetIds )
		{
			if( targetId.getTarget().equals(SecurityConstants.TARGET_EVERYTHING) )
			{
				if( targetId.getPriority() == SecurityConstants.PRIORITY_ALL_SYSTEM_SETTINGS )
				{
					results.put(targetId, s("targetallsettings"));
				}
				else if( targetId.getPriority() == SecurityConstants.PRIORITY_ALL_MANAGING )
				{
					results.put(targetId, s("targetallmanaging"));
				}
			}
		}
	}

	@Override
	public void gatherChildTargets(List<SecurityTarget> childTargets, SecurityTarget target)
	{
		if( target == null )
		{
			childTargets.add(new SecurityTarget(s("allmanaging"), Node.ALL_MANAGING, null, true));
			childTargets.add(new SecurityTarget(s("allsettings"), Node.ALL_SYSTEM_SETTINGS, null, true));
		}
	}

	private String s(String keyPart)
	{
		return CurrentLocale.get("com.tle.core.entity.services.systemsettings.securitytree." + keyPart);
	}
}
