/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.api.collection.resource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriInfo;

import com.google.common.collect.Lists;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Check;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.web.api.baseentity.serializer.BaseEntitySerializer;
import com.tle.web.api.collection.CollectionBeanSerializer;
import com.tle.web.api.collection.interfaces.CollectionResource;
import com.tle.web.api.collection.interfaces.beans.AllCollectionsSecurityBean;
import com.tle.web.api.collection.interfaces.beans.CollectionBean;
import com.tle.web.api.entity.resource.AbstractBaseEntityResource;
import com.tle.web.api.interfaces.beans.SearchBean;

@SuppressWarnings("nls")
@Bind(EquellaCollectionResource.class)
@Singleton
public class CollectionResourceImpl
	extends
		AbstractBaseEntityResource<ItemDefinition, AllCollectionsSecurityBean, CollectionBean>
	implements
		EquellaCollectionResource
{
	@Inject
	private ItemDefinitionService collectionService;
	@Inject
	private CollectionBeanSerializer collectionSerializer;

	@Override
	public SearchBean<CollectionBean> list(UriInfo uriInfo, String privilege)
	{
		final SearchBean<CollectionBean> result = new SearchBean<CollectionBean>();
		final List<CollectionBean> colbeans = Lists.newArrayList();
		final String priv = (Check.isEmpty(privilege) ? "LIST_COLLECTION" : privilege);
		final Collection<ItemDefinition> collections = aclManager.filterNonGrantedObjects(Collections.singleton(priv),
			getEntityService().enumerate());

		for( ItemDefinition col : collections )
		{
			CollectionBean cb = collectionSerializer.serialize(col, null, false);
			final Map<String, String> links = Collections.singletonMap("self", getGetUri(col.getUuid()).toString());
			cb.set("links", links);
			colbeans.add(cb);
		}
		result.setResults(colbeans);
		result.setStart(0);
		result.setAvailable(collections.size());
		result.setLength(colbeans.size());
		return result;
	}

	@Override
	protected int getSecurityPriority()
	{
		return SecurityConstants.PRIORITY_COLLECTION;
	}

	@Override
	protected Class<?> getResourceClass()
	{
		return CollectionResource.class;
	}

	@Override
	protected AbstractEntityService<?, ItemDefinition> getEntityService()
	{
		return collectionService;
	}

	@Override
	protected BaseEntitySerializer<ItemDefinition, CollectionBean> getSerializer()
	{
		return collectionSerializer;
	}

	@Override
	protected Node[] getAllNodes()
	{
		return new Node[]{Node.ALL_COLLECTIONS, Node.GLOBAL_ITEM_STATUS};
	}

	@Override
	protected AllCollectionsSecurityBean createAllSecurityBean()
	{
		return new AllCollectionsSecurityBean();
	}
}
