package com.tle.webtests.pageobject;

/**
 * For legacy entities which are not created dynamically. Do not use in new
 * tests.
 * 
 * @author Aaron
 */
public class NotPrefixedName implements PrefixedName
{
	private final String name;

	public NotPrefixedName(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj instanceof String )
		{
			return toString().equals(obj);
		}
		if( obj instanceof PrefixedName )
		{
			return toString().equals(obj.toString());
		}
		return false;
	}
}
