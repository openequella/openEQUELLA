package com.tle.core.system.impl;

import java.io.Serializable;

import com.tle.core.hibernate.dao.GenericDaoImpl;

@SuppressWarnings("nls")
public abstract class AbstractSystemDaoImpl<T, ID extends Serializable> extends GenericDaoImpl<T, ID>
{
	public AbstractSystemDaoImpl(Class<T> persistentClass)
	{
		super(persistentClass);
	}

	@Override
	protected final String getFactoryName()
	{
		return "system";
	}

	@Override
	protected final boolean isSystemDataSource()
	{
		return true;
	}
}
