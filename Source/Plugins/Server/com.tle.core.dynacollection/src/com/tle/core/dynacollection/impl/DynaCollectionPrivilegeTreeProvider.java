package com.tle.core.dynacollection.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.entity.DynaCollection;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.dynacollection.DynaCollectionService;
import com.tle.core.guice.Bind;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class DynaCollectionPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<DynaCollection>
{
	@Inject
	public DynaCollectionPrivilegeTreeProvider(DynaCollectionService dynaCollectionService)
	{
		super(dynaCollectionService, Node.ALL_DYNA_COLLECTIONS, ResourcesService.getResourceHelper(
			DynaCollectionPrivilegeTreeProvider.class).key("securitytree.alldynacollections"), Node.DYNA_COLLECTION,
			ResourcesService.getResourceHelper(DynaCollectionPrivilegeTreeProvider.class).key(
				"securitytree.targetalldynacollections"));
	}

	@Override
	protected DynaCollection createEntity()
	{
		return new DynaCollection();
	}
}
