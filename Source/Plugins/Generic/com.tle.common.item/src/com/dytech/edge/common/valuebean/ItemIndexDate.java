/*
 * Created on Jul 19, 2005
 */
package com.dytech.edge.common.valuebean;

import java.util.Date;

import com.tle.beans.item.ItemIdKey;

public class ItemIndexDate
{
	private final ItemIdKey key;
	private final Date lastIndexed;
	private final long institutionId;

	public ItemIndexDate(long id, String uuid, int version, Date lastIndexed, long institution)
	{
		this(new ItemIdKey(id, uuid, version), lastIndexed, institution);
	}

	public ItemIndexDate(ItemIdKey key, Date lastIndexed, long institutionId)
	{
		this.key = key;
		this.lastIndexed = lastIndexed;
		this.institutionId = institutionId;
	}

	public ItemIdKey getKey()
	{
		return key;
	}

	public Date getLastIndexed()
	{
		return lastIndexed;
	}

	public long getInstitutionId()
	{
		return institutionId;
	}

}
