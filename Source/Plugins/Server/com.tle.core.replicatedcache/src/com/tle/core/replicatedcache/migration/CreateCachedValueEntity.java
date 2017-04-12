package com.tle.core.replicatedcache.migration;

import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.replicatedcache.dao.CachedValue;

@SuppressWarnings("nls")
@Bind
@Singleton
public class CreateCachedValueEntity extends AbstractCreateMigration
{
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.replicatedcache.migration.info.create");
	}

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		return new TablesOnlyFilter("cached_value");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{CachedValue.class, Institution.class};
	}
}
