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