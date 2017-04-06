package com.tle.web.sections.equella.utils;

import java.util.Objects;

public class SelectedGroup
{
	protected String uuid;
	// for caching purposes
	protected String displayName;

	public SelectedGroup()
	{
	}

	public SelectedGroup(String str)
	{
		int pipeIndex = str.indexOf('|');

		if( pipeIndex >= 0 )
		{
			uuid = str.substring(0, pipeIndex);
			displayName = str.substring(pipeIndex + 1);
		}
		else
		{
			uuid = str;
			displayName = ""; //$NON-NLS-1$
		}
	}

	public SelectedGroup(String uuid, String displayName)
	{
		this.uuid = uuid;
		this.displayName = displayName;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	@Override
	public String toString()
	{
		return uuid + '|' + displayName;
	}

	@Override
	public boolean equals(Object other)
	{
		if( other instanceof SelectedGroup )
		{
			final SelectedGroup otherGroup = (SelectedGroup) other;
			return Objects.equals(uuid, otherGroup.uuid);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		if( uuid != null )
		{
			return uuid.hashCode();
		}
		return 0;
	}
}
