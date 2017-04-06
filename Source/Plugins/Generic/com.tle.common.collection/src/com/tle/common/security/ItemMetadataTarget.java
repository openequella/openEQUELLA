package com.tle.common.security;

import java.io.Serializable;
import java.util.Objects;

import com.tle.beans.entity.itemdef.ItemDefinition;

/**
 * @author Nicholas Read
 */
public class ItemMetadataTarget implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String id;
	private ItemDefinition itemDefinition;

	public ItemMetadataTarget(String id, ItemDefinition itemDefinition)
	{
		this.id = id;
		this.itemDefinition = itemDefinition;
	}

	public String getId()
	{
		return id;
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

		if( !(obj instanceof ItemMetadataTarget) )
		{
			return false;
		}

		ItemMetadataTarget rhs = (ItemMetadataTarget) obj;
		return Objects.equals(id, rhs.id) && itemDefinition.equals(rhs.itemDefinition);
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
}