package com.tle.beans.item;

import com.tle.beans.Institution;

public class ItemIdInst extends AbstractItemKey
{
	private long instId;

	public ItemIdInst(ItemId itemId, Institution inst)
	{
		this.uuid = itemId.getUuid();
		this.version = itemId.getVersion();
		this.instId = inst.getUniqueId();
	}

	@Override
	public int hashCode()
	{
		return (int) (super.hashCode() + instId);
	}

	@Override
	public boolean equals(Object obj)
	{
		return super.equals(obj) && instId == ((ItemIdInst) obj).instId;
	}
}
