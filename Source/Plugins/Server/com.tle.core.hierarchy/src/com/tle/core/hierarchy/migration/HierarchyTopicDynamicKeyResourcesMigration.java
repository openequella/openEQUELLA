package com.tle.core.hierarchy.migration;

import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.hierarchy.HierarchyTopicDynamicKeyResources;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class HierarchyTopicDynamicKeyResourcesMigration extends AbstractCreateMigration
{
	private static final String TITLE_KEY = PluginServiceImpl
		.getMyPluginId(HierarchyTopicDynamicKeyResourcesMigration.class) + ".migration.dynamic.title";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(TITLE_KEY);
	}

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		return new TablesOnlyFilter("hierarchy_topic_dynamic_key_re");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{HierarchyTopicDynamicKeyResources.class, Institution.class};
	}
}
