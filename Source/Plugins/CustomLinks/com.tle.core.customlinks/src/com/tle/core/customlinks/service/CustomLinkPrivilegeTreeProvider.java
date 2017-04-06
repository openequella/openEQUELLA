package com.tle.core.customlinks.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.customlinks.entity.CustomLink;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class CustomLinkPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<CustomLink>
{
	@Inject
	public CustomLinkPrivilegeTreeProvider(CustomLinkService customLinkService)
	{
		super(customLinkService, Node.ALL_CUSTOM_LINKS, ResourcesService.getResourceHelper(
			CustomLinkPrivilegeTreeProvider.class).key("securitytree.allcustomlinks"), Node.CUSTOM_LINK,
			ResourcesService.getResourceHelper(CustomLinkPrivilegeTreeProvider.class).key(
				"securitytree.targetallcustomlinks"));
	}

	@Override
	protected CustomLink createEntity()
	{
		return new CustomLink();
	}
}
