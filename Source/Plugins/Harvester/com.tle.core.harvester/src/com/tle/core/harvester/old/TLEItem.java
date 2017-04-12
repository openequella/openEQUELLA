package com.tle.core.harvester.old;

import java.util.Date;

/**
 * @author Nicholas Read
 */
public class TLEItem
{
	private String uuid;
	private int version;
	private Date created;
	private Date modified;

	public TLEItem(String uuid, int version, Date created, Date modified)
	{
		this.uuid = uuid;
		this.version = version;
		this.created = created;
		this.modified = modified;

	}

	public Date getCreationDate()
	{
		return created;
	}

	public String getUuid()
	{
		return uuid;
	}

	public int getVersion()
	{
		return version;
	}

	public Date getModifiedDate()
	{
		return modified;
	}
}
