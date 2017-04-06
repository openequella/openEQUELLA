package com.tle.core.userscripts.migration;

import java.util.Set;

import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.ClassDependencies;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class CreateUserScriptsSchema extends AbstractCreateMigration
{
	private static final String KEY_PFX = PluginServiceImpl.getMyPluginId(CreateUserScriptsSchema.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PFX + "migration.createentity");
	}

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		return new TablesOnlyFilter("user_script");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		final Set<Class<?>> domainClasses = Sets.newHashSet(ClassDependencies.baseEntity());
		domainClasses.add(UserScript.class);
		return domainClasses.toArray(new Class<?>[domainClasses.size()]);

	}

}
