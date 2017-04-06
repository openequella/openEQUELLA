package com.tle.core.harvester.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.harvester.HarvesterProfileService;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class HarvesterPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<HarvesterProfile>
{
	@Inject
	public HarvesterPrivilegeTreeProvider(HarvesterProfileService harvesterService)
	{
		super(harvesterService, Node.ALL_HARVESTER_PROFILES, ResourcesService.getResourceHelper(
			HarvesterPrivilegeTreeProvider.class).key("securitytree.allharvesterprofiles"), Node.HARVESTER_PROFILE,
			ResourcesService.getResourceHelper(HarvesterPrivilegeTreeProvider.class).key(
				"securitytree.targetallharvesterprofiles"));
	}

	@Override
	protected HarvesterProfile createEntity()
	{
		return new HarvesterProfile();
	}
}
