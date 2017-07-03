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

package com.tle.web.api.schema;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.entity.Schema;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.schema.service.SchemaService;
import com.tle.web.api.baseentity.serializer.BaseEntitySerializer;
import com.tle.web.api.entity.resource.AbstractBaseEntityResource;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.api.schema.interfaces.SchemaResource;
import com.tle.web.api.schema.interfaces.beans.SchemaBean;

/**
 * @author larry
 */
@Bind(SchemaResource.class)
@Singleton
public class SchemaResourceImpl extends AbstractBaseEntityResource<Schema, BaseEntitySecurityBean, SchemaBean>
	implements
		SchemaResource
{
	@Inject
	private SchemaService schemaService;
	@Inject
	private SchemaBeanSerializer serializer;

	@Override
	protected Node[] getAllNodes()
	{
		return new Node[]{Node.ALL_SCHEMAS};
	}

	@Override
	protected BaseEntitySecurityBean createAllSecurityBean()
	{
		return new BaseEntitySecurityBean();
	}

	@Override
	protected int getSecurityPriority()
	{
		return SecurityConstants.PRIORITY_SCHEMA;
	}

	@Override
	protected AbstractEntityService<?, Schema> getEntityService()
	{
		return schemaService;
	}

	@Override
	protected BaseEntitySerializer<Schema, SchemaBean> getSerializer()
	{
		return serializer;
	}

	@Override
	protected Class<?> getResourceClass()
	{
		return SchemaResource.class;
	}
}
