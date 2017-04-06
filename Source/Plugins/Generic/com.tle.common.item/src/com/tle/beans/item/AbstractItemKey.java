package com.tle.beans.item;

public abstract class AbstractItemKey implements ItemKey
{
	protected String uuid;
	protected int version;

	public AbstractItemKey(String uuid, int version)
	{
		this.uuid = uuid;
		this.version = version;
	}

	public AbstractItemKey()
	{
		// for parsers
	}

	@Override
	public String getUuid()
	{
		return uuid;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	@Override
	public final String toString()
	{
		return toString(version);
	}

	@Override
	public String toString(int version)
	{
		return uuid + '/' + version;
	}

	@Override
	public int hashCode()
	{
		return uuid.hashCode() ^ version;
	}

	@SuppressWarnings("nls")
	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( obj == null )
		{
			return false;
		}
		if( obj.getClass() != getClass() )
		{
			throw new RuntimeException("Should not be comparing different ItemKey classes");
		}
		AbstractItemKey rhs = (AbstractItemKey) obj;
		return uuid.equals(rhs.uuid) && version == rhs.version;
	}

	public boolean isDRMApplicable()
	{
		return true;
	}

}
