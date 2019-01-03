/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
