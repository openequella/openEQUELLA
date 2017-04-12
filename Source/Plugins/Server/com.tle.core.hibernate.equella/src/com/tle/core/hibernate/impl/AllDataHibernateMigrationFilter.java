package com.tle.core.hibernate.impl;

import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.mapping.AuxiliaryDatabaseObject;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;

public class AllDataHibernateMigrationFilter implements HibernateCreationFilter
{
	private boolean includeGenerators = true;

	@Override
	public boolean includeForeignKey(Table table, ForeignKey fk)
	{
		return true;
	}

	@Override
	public boolean includeGenerator(PersistentIdentifierGenerator pig)
	{
		return includeGenerators;
	}

	@Override
	public boolean includeIndex(Table table, Index index)
	{
		return true;
	}

	@Override
	public boolean includeObject(AuxiliaryDatabaseObject object)
	{
		return true;
	}

	@Override
	public boolean includeTable(Table table)
	{
		return true;
	}

	@Override
	public boolean includeUniqueKey(Table table, UniqueKey uk)
	{
		return true;
	}

	public boolean isIncludeGenerators()
	{
		return includeGenerators;
	}

	public void setIncludeGenerators(boolean includeGenerators)
	{
		this.includeGenerators = includeGenerators;
	}

}
