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

package com.tle.web.api.schema.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.devlib.PropBagEx;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.Schema;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.freetext.event.ItemReindexEvent;
import com.tle.core.freetext.reindex.SchemaFilter;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.event.ItemOperationEvent;
import com.tle.core.item.operations.BaseFilter;
import com.tle.core.item.standard.FilterFactory;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.schema.service.SchemaService;
import com.tle.web.api.baseentity.serializer.AbstractBaseEntityEditor;
import com.tle.web.api.schema.SchemaEditor;
import com.tle.web.api.schema.interfaces.beans.SchemaBean;
import com.tle.web.api.schema.interfaces.beans.SchemaNodeBean;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
public class SchemaEditorImpl extends AbstractBaseEntityEditor<Schema, SchemaBean> implements SchemaEditor
{
	@Inject
	private SchemaService schemaService;
	private boolean refreshSchemaItems;
	private boolean reindexItems;

	@AssistedInject
	public SchemaEditorImpl(@Assisted Schema schema, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("lockId") @Nullable String lockId, @Assisted("editing") boolean editing,
		@Assisted("importing") boolean importing)
	{
		super(schema, stagingUuid, lockId, editing, importing);
	}

	@AssistedInject
	public SchemaEditorImpl(@Assisted Schema schema, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("importing") boolean importing)
	{
		this(schema, stagingUuid, null, false, importing);
	}

	@Override
	protected void copyCustomFields(SchemaBean bean)
	{
		super.copyCustomFields(bean);

		final Schema schema = entity;
		if( editing && (!Objects.equal(schema.getItemNamePath(), bean.getNamePath())
			|| !Objects.equal(schema.getItemDescriptionPath(), bean.getDescriptionPath())) )
		{
			refreshSchemaItems = true;
		}
		schema.setItemNamePath(bean.getNamePath());
		schema.setItemDescriptionPath(bean.getDescriptionPath());

		final Set<String> oldIndexedPaths = getIndexedPaths(schema);
		final Map<String, SchemaNodeBean> def = bean.getDefinition();
		if( def != null )
		{
			if( def.size() > 1 )
			{
				throw new RuntimeException("Cannot have more than 1 root node");
			}
			final List<TSchemaNode> isn = buildNodeTree(null, def);
			final String serialDef = isn.get(0).getXml().toString();
			schema.setSerialisedDefinition(serialDef);
		}
		else
		{
			schema.setSerialisedDefinition("<xml/>");
		}
		final Set<String> newIndexedPaths = getIndexedPaths(schema);
		if( !Objects.equal(oldIndexedPaths, newIndexedPaths) )
		{
			reindexItems = true;
		}
	}

	@Nullable
	private Set<String> getIndexedPaths(Schema s)
	{
		if( !editing || s.getSerialisedDefinition() == null )
		{
			return null;
		}
		Set<String> rv = new HashSet<String>();
		getIndexedPaths(rv, new PropBagEx(s.getSerialisedDefinition()), "");
		return rv;
	}

	private void getIndexedPaths(Set<String> rv, PropBagEx xml, String path)
	{
		for( PropBagEx sxml : xml.iterator() )
		{
			final String spath = path + '/' + sxml.getNodeName();

			if( sxml.isNodeTrue("@field") )
			{
				rv.add("f" + spath);
			}

			if( sxml.isNodeTrue("@search") )
			{
				rv.add("s" + spath);
			}

			getIndexedPaths(rv, sxml, spath);
		}
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	protected void afterFinishedEditing()
	{
		super.afterFinishedEditing();

		// Refresh item name and description if either of the XPaths has changed
		if( editing && refreshSchemaItems )
		{
			publishEventAfterCommit(new ItemOperationEvent(
				new FactoryMethodLocator<BaseFilter>(FilterFactory.class, "refreshSchemaItems", entity.getId())));
			return;
		}

		// Re-index items if the indexing settings for schema nodes has changed
		if( editing && reindexItems )
		{
			publishEvent(new ItemReindexEvent(new SchemaFilter(entity)));
			return;
		}
	}

	@Nullable
	private List<TSchemaNode> buildNodeTree(@Nullable TSchemaNode parent, @Nullable Map<String, SchemaNodeBean> def)
	{
		if( def == null )
		{
			return null;
		}

		final List<TSchemaNode> nodes = Lists.newArrayList();
		for( Map.Entry<String, SchemaNodeBean> defEntry : def.entrySet() )
		{
			final String key = defEntry.getKey();
			final SchemaNodeBean bean = defEntry.getValue();

			final TSchemaNode node = new TSchemaNode();
			node.setName(key);
			node.setAttribute(bean.isNested());
			node.setFieldIndexed(bean.isField());
			node.setIndexedInBody(bean.isIndexed());
			node.setType(bean.getType());
			buildNodeTree(node, bean.getChildren());
			nodes.add(node);
		}

		if( parent != null )
		{
			parent.setChildNodes(nodes);
		}
		return nodes;
	}

	@Override
	protected AbstractEntityService<?, Schema> getEntityService()
	{
		return schemaService;
	}

	@NonNullByDefault(false)
	private static class TSchemaNode
	{
		private List<TSchemaNode> childNodes;
		private String name;
		private boolean attribute;
		private boolean fieldIndexed;
		private boolean indexedInBody;
		private String type;

		public void setName(String name)
		{
			this.name = name;
		}

		public void setAttribute(boolean attribute)
		{
			this.attribute = attribute;
		}

		public void setFieldIndexed(boolean fieldIndexed)
		{
			this.fieldIndexed = fieldIndexed;
		}

		public void setIndexedInBody(boolean indexedInBody)
		{
			this.indexedInBody = indexedInBody;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public void setChildNodes(List<TSchemaNode> childNodes)
		{
			this.childNodes = childNodes;
		}

		public List<TSchemaNode> getChildNodes()
		{
			return childNodes;
		}

		public boolean hasChildren()
		{
			List<TSchemaNode> children = getChildNodes();
			return !(children == null || children.size() == 0);
		}

		public boolean isAttribute()
		{
			return attribute;
		}

		public boolean isFieldIndexed()
		{
			return fieldIndexed;
		}

		public boolean isIndexedInBody()
		{
			return indexedInBody;
		}

		public PropBagEx getXml()
		{
			String nodeName = name;
			if( nodeName.startsWith("@") )
			{
				nodeName = nodeName.substring(1);
			}
			PropBagEx xml = new PropBagEx().newSubtree(nodeName);

			if( isAttribute() )
			{
				xml.setNode("@attribute", true);
			}

			if( !hasNonAttributeChildren() )
			{
				if( isIndexedInBody() )
				{
					xml.setNode("@search", true);
				}

				if( isFieldIndexed() )
				{
					xml.setNode("@field", true);
				}
				if( type != null )
				{
					xml.setNode("@type", type);
				}
			}
			final int count = getChildCount();
			for( int i = 0; i < count; i++ )
			{
				TSchemaNode child = getChildAt(i);
				PropBagEx childXml = child.getXml();
				xml.append("/", childXml);
			}
			return xml;
		}

		public boolean hasNonAttributeChildren()
		{
			int size = getChildCount();
			for( int i = 0; i < size; i++ )
			{
				TSchemaNode n = getChildAt(i);
				if( !n.isAttribute() )
				{
					return true;
				}
			}
			return false;
		}

		public TSchemaNode getChildAt(int index)
		{
			return getChildNodes().get(index);
		}

		public int getChildCount()
		{
			if( !hasChildren() )
			{
				return 0;
			}
			return getChildNodes().size();
		}
	}

	@BindFactory
	public interface SchemaEditorFactory
	{
		SchemaEditorImpl createExistingEditor(@Assisted Schema schema,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
			@Assisted("editing") boolean editing, @Assisted("importing") boolean importing);

		SchemaEditorImpl createNewEditor(@Assisted Schema schema, @Assisted("stagingUuid") @Nullable String stagingUuid,
			@Assisted("importing") boolean importing);
	}
}
