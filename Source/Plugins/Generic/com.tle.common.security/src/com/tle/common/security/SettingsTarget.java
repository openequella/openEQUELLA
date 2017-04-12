package com.tle.common.security;

import java.io.Serializable;

/**
 * @author Nicholas Read
 */
public class SettingsTarget implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String id;

	public SettingsTarget(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof SettingsTarget) )
		{
			return false;
		}

		return id == ((SettingsTarget) obj).id;
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
}