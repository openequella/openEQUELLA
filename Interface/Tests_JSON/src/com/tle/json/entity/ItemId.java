package com.tle.json.entity;

import java.util.Objects;

public class ItemId
{
	private final int version;
	private final String uuid;

	public ItemId(String uuid, int version)
	{
		this.uuid = uuid;
		this.version = version;
	}

	public int getVersion()
	{
		return version;
	}

	public String getUuid()
	{
		return uuid;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(uuid, version);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ItemId))
		{
			return false;
		}
		ItemId other = (ItemId) obj;
		return other.uuid.equals(uuid) && version == other.version;
	}

	@Override
	public String toString()
	{
		return uuid + "/" + version;
	}
}
