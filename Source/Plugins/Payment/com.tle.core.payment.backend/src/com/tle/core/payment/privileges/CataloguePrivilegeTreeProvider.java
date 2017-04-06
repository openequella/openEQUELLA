package com.tle.core.payment.privileges;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.payment.entity.Catalogue;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class CataloguePrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<Catalogue>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(CataloguePrivilegeTreeProvider.class);

	@Inject
	public CataloguePrivilegeTreeProvider(CatalogueService regionService)
	{
		super(regionService, Node.ALL_CATALOGUES, resources.key("securitytree.allcatalogues"), Node.CATALOGUE,
			resources.key("securitytree.targetallcatalogues"));
	}

	@Override
	protected Catalogue createEntity()
	{
		return new Catalogue();
	}
}
