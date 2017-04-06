package com.tle.core.pss.migration;

import java.util.Set;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.ClassDependencies;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.pss.entity.PssCallbackLog;

@SuppressWarnings("nls")
@Bind
@Singleton
public class CreatePearsonScormServicesEntities extends AbstractCreateMigration
{
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.pss.migration.info.create");
	}

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		return new TablesOnlyFilter("pss_callback_log");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		Set<Class<?>> deps = ClassDependencies.item();
		deps.add(PssCallbackLog.class);
		return deps.toArray(new Class[deps.size()]);
	}
}
