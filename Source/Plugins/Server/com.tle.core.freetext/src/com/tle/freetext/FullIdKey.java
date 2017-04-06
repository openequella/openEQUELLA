package com.tle.freetext;

import com.dytech.edge.common.valuebean.ItemIndexDate;

public class FullIdKey
{
	private final long id;
	private final long instId;

	public FullIdKey(ItemIndexDate dkey)
	{
		this.id = dkey.getKey().getKey();
		this.instId = dkey.getInstitutionId();
	}

	public FullIdKey(long id, long instId)
	{
		this.id = id;
		this.instId = instId;
	}

	@Override
	public int hashCode()
	{
		return (int) ((id ^ (id >>> 32)) ^ (instId ^ (instId >>> 32)));
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof FullIdKey) )
		{
			return false;
		}

		FullIdKey other = (FullIdKey) obj;
		return other.id == id && other.instId == instId;
	}
}