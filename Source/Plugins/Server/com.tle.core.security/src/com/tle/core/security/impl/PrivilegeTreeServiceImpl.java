package com.tle.core.security.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Maps;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.PrivilegeTreeProvider;
import com.tle.core.security.PrivilegeTreeService;

@Bind(PrivilegeTreeService.class)
@Singleton
@SuppressWarnings("nls")
public class PrivilegeTreeServiceImpl implements PrivilegeTreeService
{
	private PluginTracker<PrivilegeTreeProvider> providers;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		providers = new PluginTracker<PrivilegeTreeProvider>(pluginService, "com.tle.core.security",
			"privilegeTreeProviders", null);
		providers.setBeanKey("provider");
	}

	@Override
	public Map<TargetId, String> mapTargetIdsToNames(Collection<TargetId> targetIds)
	{
		Map<TargetId, String> rv = Maps.newHashMap();
		for( PrivilegeTreeProvider provider : providers.getBeanList() )
		{
			provider.mapTargetIdsToNames(targetIds, rv);

			targetIds.removeAll(rv.keySet());
			if( targetIds.isEmpty() )
			{
				break;
			}
		}
		return rv;
	}

	@Override
	public List<SecurityTarget> getChildTargets(SecurityTarget target)
	{
		List<SecurityTarget> rv = new ArrayList<SecurityTarget>();
		for( PrivilegeTreeProvider provider : providers.getBeanList() )
		{
			provider.gatherChildTargets(rv, target);
		}
		return rv;
	}
}
