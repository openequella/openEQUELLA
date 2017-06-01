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

package com.tle.common.hierarchy;

import java.util.List;

import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.SchemaScript;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.common.search.searchset.SearchSet;

public class SearchSetAdapter implements SearchSet
{
	private final HierarchyTopic delegate;

	public SearchSetAdapter(HierarchyTopic delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public String getFreetextQuery()
	{
		return delegate.getFreetext();
	}

	@Override
	public List<ItemDefinitionScript> getInheritedItemDefs()
	{
		return delegate.getInheritedItemDefs();
	}

	@Override
	public List<SchemaScript> getInheritedSchemas()
	{
		return delegate.getInheritedSchemas();
	}

	@Override
	public List<ItemDefinitionScript> getItemDefs()
	{
		return delegate.getAdditionalItemDefs();
	}

	@Override
	public String getId()
	{
		return delegate.getUuid();
	}

	@Override
	public SearchSet getParent()
	{
		HierarchyTopic p = delegate.getParent();
		return p != null ? new SearchSetAdapter(p) : null;
	}

	@Override
	public List<SchemaScript> getSchemas()
	{
		return delegate.getAdditionalSchemas();
	}

	@Override
	public String getVirtualisationPath()
	{
		return delegate.getVirtualisationPath();
	}

	@Override
	public String getVirtualiserPluginId()
	{
		return delegate.getVirtualisationId();
	}

	@Override
	public boolean isInheritFreetext()
	{
		return delegate.isInheritFreetext();
	}

	@Override
	public void setFreetextQuery(String query)
	{
		delegate.setFreetext(query);
	}

	@Override
	public void setInheritFreetext(boolean inheritFreetext)
	{
		delegate.setInheritFreetext(inheritFreetext);
	}

	@Override
	public void setInheritedItemDefs(List<ItemDefinitionScript> itemDefs)
	{
		delegate.setInheritedItemDefs(itemDefs);
	}

	@Override
	public void setInheritedSchemas(List<SchemaScript> schemas)
	{
		delegate.setInheritedSchemas(schemas);
	}

	@Override
	public void setItemDefs(List<ItemDefinitionScript> itemDefs)
	{
		delegate.setAdditionalItemDefs(itemDefs);
	}

	@Override
	public void setSchemas(List<SchemaScript> schemas)
	{
		delegate.setAdditionalSchemas(schemas);
	}

	@Override
	public void setVirtualisationPath(String path)
	{
		delegate.setVirtualisationPath(path);
	}

	@Override
	public void setVirtualiserPluginId(String pluginId)
	{
		delegate.setVirtualisationId(pluginId);
	}

	@Override
	public String getAttribute(String key)
	{
		return delegate.getAttribute(key);
	}

	@Override
	public void removeAttribute(String key)
	{
		delegate.removeAttribute(key);
	}

	@Override
	public void setAttribute(String key, String value)
	{
		delegate.setAttribute(key, value);
	}

	@Override
	public int hashCode()
	{
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}
		else if( obj == null || !SearchSetAdapter.class.isAssignableFrom(obj.getClass()) )
		{
			return false;
		}
		else
		{
			return delegate.equals(((SearchSetAdapter) obj).delegate);
		}
	}
}
