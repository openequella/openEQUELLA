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

package com.tle.common.security;

import java.io.Serializable;
import java.util.Objects;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemStatus;

/**
 * @author Nicholas Read
 */
public class ItemStatusTarget implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final ItemStatus itemStatus;
	private ItemDefinition itemDefinition;

	public ItemStatusTarget(ItemStatus itemStatus, ItemDefinition itemDefinition)
	{
		this.itemStatus = itemStatus;
		this.itemDefinition = itemDefinition;
	}

	public ItemStatus getItemStatus()
	{
		return itemStatus;
	}

	public ItemDefinition getItemDefinition()
	{
		return itemDefinition;
	}

	public void setItemDefinition(ItemDefinition itemDefinition)
	{
		this.itemDefinition = itemDefinition;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof ItemStatusTarget) )
		{
			return false;
		}

		ItemStatusTarget rhs = (ItemStatusTarget) obj;
		return getItemStatus().equals(rhs.getItemStatus())
			&& Objects.equals(itemDefinition, rhs.getItemDefinition());
	}

	@Override
	public int hashCode()
	{
		return getItemStatus().hashCode();
	}
}