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

package com.tle.web.api.collection.resource;

import com.tle.beans.entity.itemdef.ItemDefinition;
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

import javax.inject.Inject;
import javax.inject.Singleton;

@SuppressWarnings("nls")
@Bind(CollectionResource.class)
@Singleton
public class CollectionResourceImpl
	extends
		AbstractBaseEntityResource<ItemDefinition, AllCollectionsSecurityBean, CollectionBean>
		implements CollectionResource
{
	@Inject
	private ItemDefinitionService collectionService;
	@Inject
	private CollectionBeanSerializer collectionSerializer;

	@Override
	protected Class<?> getResourceClass()
	{
		return CollectionResource.class;
	}

	@Override
	public AbstractEntityService<?, ItemDefinition> getEntityService()
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
