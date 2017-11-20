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

package com.tle.beans.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.CollectionOfElements;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.schema.Citation;
import com.tle.common.Check;

@Entity
@AccessType("field")
public class Schema extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	@Transient
	private transient PropBagEx definition;
	@Transient
	private transient SchemaNode rootSchemaNode;

	@CollectionOfElements(fetch = FetchType.LAZY)
	private List<SchemaTransform> expTransforms = new ArrayList<SchemaTransform>();

	@CollectionOfElements(fetch = FetchType.LAZY)
	private List<SchemaTransform> impTransforms = new ArrayList<SchemaTransform>();

	private String itemNamePath;
	private String itemDescriptionPath;

	@Lob
	private String serialisedDefinition;

	@CollectionOfElements(fetch = FetchType.LAZY)
	@JoinColumn
	private List<Citation> citations = new ArrayList<Citation>();

	public Schema()
	{
		super();
	}

	public Schema(long id)
	{
		this();
		setId(id);
	}

	/**
	 * See <code>withDefinition(...)</code> for usage in threaded environments
	 */
	public synchronized PropBagEx getDefinitionNonThreadSafe()
	{
		if( definition == null && serialisedDefinition != null )
		{
			definition = new PropBagEx(serialisedDefinition);
		}
		return definition;
	}

	public synchronized void setDefinition(PropBagEx definition)
	{
		this.definition = definition;
		setSerialisedDefinition(definition != null ? definition.toString() : null);
	}

	public synchronized String getSerialisedDefinition()
	{
		return serialisedDefinition;
	}

	public synchronized void setSerialisedDefinition(String serialisedDefinition)
	{
		this.serialisedDefinition = serialisedDefinition;
	}

	public List<SchemaTransform> getImportTransforms()
	{
		return impTransforms;
	}

	public void setImportTransforms(List<SchemaTransform> transforms)
	{
		this.impTransforms = transforms;
	}

	public List<SchemaTransform> getExportTransforms()
	{
		return expTransforms;
	}

	public void setExportTransforms(List<SchemaTransform> transforms)
	{
		this.expTransforms = transforms;
	}

	public Map<String, String> getImportTransformsMap()
	{
		return getTransformsMap(impTransforms);
	}

	public Map<String, String> getExportTransformsMap()
	{
		return getTransformsMap(expTransforms);
	}

	private Map<String, String> getTransformsMap(List<SchemaTransform> transforms)
	{
		Map<String, String> results = new HashMap<String, String>();
		if( !Check.isEmpty(transforms) )
		{
			for( SchemaTransform transform : transforms )
			{
				results.put(transform.getType(), transform.getFilename());
			}
		}
		return results;
	}

	public String getItemDescriptionPath()
	{
		return itemDescriptionPath;
	}

	public void setItemDescriptionPath(String itemDescriptionPath)
	{
		this.itemDescriptionPath = itemDescriptionPath;
	}

	public String getItemNamePath()
	{
		return itemNamePath;
	}

	public void setItemNamePath(String itemNamePath)
	{
		this.itemNamePath = itemNamePath;
	}

	public List<Citation> getCitations()
	{
		return citations;
	}

	public void setCitations(List<Citation> citations)
	{
		this.citations = citations;
	}

	// Blocking access to the schema definition //////////////////////////////

	public synchronized <T> T withDefinition(WithDefinition<T> wd)
	{
		return wd.execute(getDefinitionNonThreadSafe());
	}

	public interface WithDefinition<T>
	{
		T execute(PropBagEx schema);
	}

	public static class CloneDefinition implements WithDefinition<PropBagEx>
	{
		@Override
		public PropBagEx execute(PropBagEx xml)
		{
			return (PropBagEx) xml.clone();
		}
	}

	// Concurrent, thread-safe traversal of schema nodes value ///////////////

	public synchronized SchemaNode getRootSchemaNode()
	{
		if( rootSchemaNode == null )
		{
			rootSchemaNode = withDefinition(new WithDefinition<SchemaNode>()
			{
				@Override
				public SchemaNode execute(PropBagEx xml)
				{
					return new ItemSchemaNode(xml.getRootElement());
				}
			});
		}
		return rootSchemaNode;
	}

	public interface SchemaNode
	{
		List<SchemaNode> getChildNodes();

		boolean hasChildren();

		boolean isAttribute();

		boolean isXpathIndexed();

		boolean isFieldIndexed();

		boolean isIndexedInBody();

		String getName();

		String getType();
	}

	@SuppressWarnings("nls")
	public static class ItemSchemaNode implements SchemaNode
	{
		private final List<SchemaNode> nodes;

		private final String name;
		private final boolean attribute;
		private final String type;
		private final boolean fieldIndexed;
		private final boolean indexedInBody;

		public ItemSchemaNode(Element elem)
		{
			String nodeName = elem.getNodeName();
			attribute = "true".equals(elem.getAttribute("attribute"));
			name = attribute ? '@' + nodeName : nodeName;
			nodes = new ArrayList<SchemaNode>();
			for( Node child = elem.getFirstChild(); child != null; child = child.getNextSibling() )
			{
				if( child.getNodeType() == Node.ELEMENT_NODE )
				{
					Element childElement = (Element) child;
					nodes.add(new ItemSchemaNode(childElement));
				}
			}
			fieldIndexed = "true".equals(elem.getAttribute("field"));
			indexedInBody = "true".equals(elem.getAttribute("search"));
			type = elem.getAttribute("type");
		}

		@Override
		public List<SchemaNode> getChildNodes()
		{
			return nodes;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public String getType()
		{
			return type;
		}

		@Override
		public boolean hasChildren()
		{
			return !nodes.isEmpty();
		}

		@Override
		public boolean isAttribute()
		{
			return attribute;
		}

		@Override
		public boolean isFieldIndexed()
		{
			return fieldIndexed;
		}

		@Override
		public boolean isIndexedInBody()
		{
			return indexedInBody;
		}

		@Override
		public boolean isXpathIndexed()
		{
			return hasChildren();
		}
	}
}
