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