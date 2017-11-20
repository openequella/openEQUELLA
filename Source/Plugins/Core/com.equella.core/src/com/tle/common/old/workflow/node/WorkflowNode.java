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

package com.tle.common.old.workflow.node;

import java.io.Serializable;
import java.util.UUID;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.tle.beans.entity.LanguageBundle;

public abstract class WorkflowNode implements Serializable
{
	private static final long serialVersionUID = 1;

	public static final int ROOT_TYPE = -1;
	public static final int PARALLEL_TYPE = 0;
	public static final int SERIAL_TYPE = 1;
	public static final int DECISION_TYPE = 2;
	public static final int ITEM_TYPE = 3;

	public static final String PARALLEL_NODE_TYPE = "parallel"; //$NON-NLS-1$
	public static final String SERIAL_NODE_TYPE = "serial"; //$NON-NLS-1$
	public static final String DECISION_NODE_TYPE = "decision"; //$NON-NLS-1$
	public static final String ITEM_NODE_TYPE = "item"; //$NON-NLS-1$

	private String id;
	protected LanguageBundle name;
	protected int type;
	protected WorkflowTreeNode parent;

	public WorkflowNode()
	{
		type = getDefaultType();
	}

	public WorkflowNode(LanguageBundle name)
	{
		this();
		this.name = name;
		id = UUID.randomUUID().toString();
	}

	public static BiMap<String, Integer> getTypeMap()
	{
		BiMap<String, Integer> typeMap = HashBiMap.create();
		typeMap.put(ITEM_NODE_TYPE, ITEM_TYPE);
		typeMap.put(PARALLEL_NODE_TYPE, PARALLEL_TYPE);
		typeMap.put(SERIAL_NODE_TYPE, SERIAL_TYPE);
		typeMap.put(DECISION_NODE_TYPE, DECISION_TYPE);
		return typeMap;
	}

	protected abstract int getDefaultType();

	public void setParentObject(Object o)
	{
		if( o instanceof WorkflowTreeNode )
		{
			this.parent = (WorkflowTreeNode) o;
		}
	}

	public WorkflowTreeNode getParent()
	{
		return parent;
	}

	public void setName(LanguageBundle name)
	{
		this.name = name;
	}

	public LanguageBundle getName()
	{
		return name;
	}

	public int getType()
	{
		return type;
	}

	public boolean canAddChildren()
	{
		return false;
	}

	public boolean isLeafNode()
	{
		return true;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}
		if( obj == null )
		{
			return false;
		}
		if( getClass() != obj.getClass() )
		{
			return false;
		}
		final WorkflowNode other = (WorkflowNode) obj;
		if( id == null )
		{
			if( other.id != null )
			{
				return false;
			}
		}
		else if( !id.equals(other.id) )
		{
			return false;
		}
		return true;
	}
}
