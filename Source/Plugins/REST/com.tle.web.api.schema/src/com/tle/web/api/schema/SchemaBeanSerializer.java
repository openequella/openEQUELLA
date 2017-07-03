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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.Schema.SchemaNode;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.schema.service.SchemaService;
import com.tle.web.api.baseentity.serializer.AbstractEquellaBaseEntitySerializer;
import com.tle.web.api.schema.beans.EquellaSchemaBean;
import com.tle.web.api.schema.impl.SchemaEditorImpl.SchemaEditorFactory;
import com.tle.web.api.schema.interfaces.beans.SchemaBean;
import com.tle.web.api.schema.interfaces.beans.SchemaNodeBean;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
@Singleton
public class SchemaBeanSerializer extends AbstractEquellaBaseEntitySerializer<Schema, SchemaBean, SchemaEditor>
{
	@Inject
	private SchemaService schemaService;
	@Inject
	private SchemaEditorFactory schemaEditoryFactory;

	@Override
	protected SchemaBean createBean()
	{
		return new EquellaSchemaBean();
	}

	@Override
	protected Schema createEntity()
	{
		return new Schema();
	}

	@Override
	protected SchemaEditor createExistingEditor(Schema entity, String stagingUuid, String lockId, boolean importing)
	{
		return schemaEditoryFactory.createExistingEditor(entity, stagingUuid, lockId, true, importing);
	}

	@Override
	protected SchemaEditor createNewEditor(Schema entity, String stagingUuid, boolean importing)
	{
		return schemaEditoryFactory.createNewEditor(entity, stagingUuid, importing);
	}

	@Override
	protected void copyCustomFields(Schema schema, SchemaBean sbean, Object data)
	{
		EquellaSchemaBean bean = (EquellaSchemaBean) sbean;
		bean.setNamePath(schema.getItemNamePath());
		bean.setDescriptionPath(schema.getItemDescriptionPath());
		// Map<String, String> attrs = schema.getAttributes();
		// if( attrs != null )
		// {
		// for( String key: attrs.keySet() )
		// {
		// bean.set(key, attrs.get(key));
		// }
		// }
		bean.setCitations(schema.getCitations());
		bean.setExportTransformsMap(schema.getExportTransformsMap());
		bean.setImportTransformsMap(schema.getImportTransformsMap());
		bean.setOwnerUuid(schema.getOwner());
		/* schema.getRootSchemaNode() */
		bean.setSerializedDefinition(schema.getSerialisedDefinition());

		bean.setDefinition(buildNodeBeanTree(null, Collections.singletonList(schema.getRootSchemaNode())));
	}

	@Nullable
	private Map<String, SchemaNodeBean> buildNodeBeanTree(@Nullable SchemaNodeBean parent,
		@Nullable List<SchemaNode> nodes)
	{
		if( nodes == null )
		{
			return null;
		}
		final Map<String, SchemaNodeBean> def = Maps.newHashMap();
		for( SchemaNode node : nodes )
		{
			final SchemaNodeBean nodeBean = new SchemaNodeBean();
			nodeBean.setField(node.isFieldIndexed());
			nodeBean.setIndexed(node.isIndexedInBody());
			nodeBean.setNested(node.isAttribute());
			String type = node.getType();
			if( Strings.isNullOrEmpty(type) )
			{
				nodeBean.setType(null);
			}
			else
			{
				nodeBean.setType(type);
			}
			nodeBean.addAll(buildNodeBeanTree(nodeBean, node.getChildNodes()));
			String nodeName = node.getName();
			if( node.isAttribute() && nodeName.charAt(0) != '@' )
			{
				nodeName = "@" + nodeName;
			}
			def.put(nodeName, nodeBean);
		}
		return def;
	}

	@Override
	protected AbstractEntityService<?, Schema> getEntityService()
	{
		return schemaService;
	}

	@Override
	protected Node getNonVirtualNode()
	{
		return Node.SCHEMA;
	}
}
