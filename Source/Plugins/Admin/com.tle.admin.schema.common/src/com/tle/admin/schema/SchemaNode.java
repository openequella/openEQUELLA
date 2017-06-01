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

package com.tle.admin.schema;

import java.util.Objects;

import javax.swing.tree.DefaultMutableTreeNode;

import com.dytech.devlib.PropBagEx;

/**
 * @author Nicholas Read
 */
public class SchemaNode extends DefaultMutableTreeNode
{
	private static final long serialVersionUID = 1L;
	private String name;
	private String type;
	private boolean attribute;
	private boolean searchable;
	private boolean field;
	private int lockCount;

	public SchemaNode(String name)
	{
		this.name = name;
		lockCount = 0;
		type = "text"; //$NON-NLS-1$
	}

	public String getXmlPath()
	{
		if( isRoot() )
		{
			return ""; //$NON-NLS-1$
		}
		else
		{
			String base = ((SchemaNode) getParent()).getXmlPath() + '/';
			if( isAttribute() )
			{
				base += '@';
			}
			return base + getName();
		}
	}

	public PropBagEx getXml()
	{
		PropBagEx xml = new PropBagEx().newSubtree(name);

		if( isAttribute() )
		{
			xml.setNode("@attribute", true); //$NON-NLS-1$
		}

		if( !hasNonAttributeChildren() )
		{
			if( isSearchable() )
			{
				xml.setNode("@search", true); //$NON-NLS-1$
			}

			if( isField() )
			{
				xml.setNode("@field", true); //$NON-NLS-1$
			}

			xml.setNode("@type", type); //$NON-NLS-1$
		}
		final int count = getChildCount();
		for( int i = 0; i < count; i++ )
		{
			SchemaNode child = (SchemaNode) getChildAt(i);
			PropBagEx childXml = child.getXml();
			xml.append("/", childXml); //$NON-NLS-1$
		}
		return xml;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getType()
	{
		return type;
	}

	public boolean isSearchable()
	{
		return searchable;
	}

	public void setSearchable(boolean searchable)
	{
		this.searchable = searchable;
	}

	public boolean isLocked()
	{
		if( getLockCount() > 0 )
		{
			return true;
		}
		else
		{
			final int count = getChildCount();
			for( int i = 0; i < count; i++ )
			{
				SchemaNode n = (SchemaNode) getChildAt(i);
				if( n.isLocked() )
				{
					return true;
				}
			}
			return false;
		}
	}

	public void lock()
	{
		lockCount++;
	}

	public void unlock()
	{
		lockCount--;
		if( lockCount < 0 )
		{
			lockCount = 0;
		}
	}

	public int getLockCount()
	{
		return lockCount;
	}

	@Override
	public String toString()
	{
		return getXmlPath();
	}

	public boolean hasChild(String name)
	{
		final int count = getChildCount();
		for( int i = 0; i < count; i++ )
		{
			SchemaNode n = (SchemaNode) getChildAt(i);
			if( n.getName().equals(name) )
			{
				return true;
			}
		}
		return false;
	}

	public boolean hasNonAttributeChildren()
	{
		int size = getChildCount();
		for( int i = 0; i < size; i++ )
		{
			SchemaNode n = (SchemaNode) getChildAt(i);
			if( !n.isAttribute() )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof SchemaNode) )
		{
			return false;
		}

		return Objects.equals(name, ((SchemaNode) obj).name);
	}

	public boolean isAttribute()
	{
		return attribute;
	}

	public void setAttribute(boolean attribute)
	{
		this.attribute = attribute;
	}

	public boolean isField()
	{
		return field;
	}

	public void setField(boolean field)
	{
		this.field = field;
	}
}
