package com.tle.core.payment.privileges;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.payment.entity.Region;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.payment.service.RegionService;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RegionPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<Region>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(RegionPrivilegeTreeProvider.class);

	@Inject
	public RegionPrivilegeTreeProvider(RegionService regionService)
	{
		super(regionService, Node.ALL_REGIONS, resources.key("securitytree.allregions"), Node.REGION, resources
			.key("securitytree.targetallregions"));
	}

	@Override
	protected Region createEntity()
	{
		return new Region();
	}
}
