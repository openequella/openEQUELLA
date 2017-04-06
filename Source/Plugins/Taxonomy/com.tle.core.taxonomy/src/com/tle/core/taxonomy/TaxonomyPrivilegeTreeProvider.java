package com.tle.core.taxonomy;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.core.guice.Bind;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class TaxonomyPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<Taxonomy>
{
	@Inject
	public TaxonomyPrivilegeTreeProvider(TaxonomyService taxonomyService)
	{
		super(taxonomyService, Node.ALL_TAXONOMIES, ResourcesService.getResourceHelper(
			TaxonomyPrivilegeTreeProvider.class).key("securitytree.alltaxonomies"), Node.TAXONOMY, ResourcesService
			.getResourceHelper(TaxonomyPrivilegeTreeProvider.class).key("securitytree.targetalltaxonomies"));
	}

	@Override
	protected Taxonomy createEntity()
	{
		return new Taxonomy();
	}
}
