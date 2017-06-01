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

package com.tle.core.security.impl;

import java.io.Serializable;
import java.util.Objects;

import com.tle.beans.item.Item;

public class ItemDynamicMetadataTarget implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String id;
	private Item item;

	public ItemDynamicMetadataTarget(Item item)
	{
		this.id = item.getUuid();
		this.item = item;
	}

	public String getId()
	{
		return id;
	}

	public Item getItem()
	{
		return item;
	}

	public void setItemDefinition(Item item)
	{
		this.item = item;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof ItemDynamicMetadataTarget) )
		{
			return false;
		}

		ItemDynamicMetadataTarget rhs = (ItemDynamicMetadataTarget) obj;
		return Objects.equals(id, rhs.id) && item.equals(rhs.item);
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
}