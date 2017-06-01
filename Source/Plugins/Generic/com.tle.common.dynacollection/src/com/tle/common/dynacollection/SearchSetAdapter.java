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

package com.tle.common.dynacollection;

import java.util.List;

import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.SchemaScript;
import com.tle.beans.entity.DynaCollection;
import com.tle.common.search.searchset.SearchSet;

public class SearchSetAdapter implements SearchSet
{
	private final DynaCollection delegate;

	public SearchSetAdapter(DynaCollection delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public String getFreetextQuery()
	{
		return delegate.getFreetextQuery();
	}

	@Override
	public void setFreetextQuery(String query)
	{
		delegate.setFreetextQuery(query);
	}

	@Override
	public List<ItemDefinitionScript> getItemDefs()
	{
		return delegate.getItemDefs();
	}

	@Override
	public void setItemDefs(List<ItemDefinitionScript> itemDefs)
	{
		delegate.setItemDefs(itemDefs);
	}

	@Override
	public List<SchemaScript> getSchemas()
	{
		return delegate.getSchemas();
	}

	@Override
	public void setSchemas(List<SchemaScript> schemas)
	{
		delegate.setSchemas(schemas);
	}

	@Override
	public String getVirtualisationPath()
	{
		return delegate.getVirtualisationPath();
	}

	@Override
	public void setVirtualisationPath(String path)
	{
		delegate.setVirtualisationPath(path);
	}

	@Override
	public String getVirtualiserPluginId()
	{
		return delegate.getVirtualisationId();
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
	public void setAttribute(String key, String value)
	{
		delegate.setAttribute(key, value);
	}

	@Override
	public void removeAttribute(String key)
	{
		delegate.removeAttribute(key);
	}

	// // Dynamic Collections do not support inheritance //////////////////////

	@Override
	public List<ItemDefinitionScript> getInheritedItemDefs()
	{
		return null;
	}

	@Override
	public void setInheritedItemDefs(List<ItemDefinitionScript> itemDefs)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<SchemaScript> getInheritedSchemas()
	{
		return null;
	}

	@Override
	public void setInheritedSchemas(List<SchemaScript> schemas)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getId()
	{
		return delegate.getUuid();
	}

	@Override
	public SearchSet getParent()
	{
		return null;
	}

	@Override
	public boolean isInheritFreetext()
	{
		return false;
	}

	@Override
	public void setInheritFreetext(boolean inheritFreetext)
	{
		throw new UnsupportedOperationException();
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
