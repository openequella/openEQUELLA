package com.tle.beans;

public abstract class EntityCloneable implements IdCloneable, Cloneable
{

	@SuppressWarnings("unchecked")
	public <T extends EntityCloneable> T databaseClone()
	{
		try
		{
			T cloned = (T) super.clone();
			cloned.setId(0);
			return cloned;
		}
		catch( CloneNotSupportedException e )
		{
			throw new RuntimeException(e);
		}
	}
}
