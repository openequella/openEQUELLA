package com.tle.core.integration.migration;

import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.integration.beans.AuditLogLms;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class CreateLogTable extends AbstractCreateMigration
{
	private static final String KEY_PFX = PluginServiceImpl.getMyPluginId(CreateLogTable.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PFX + "migration");
	}

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		return new TablesOnlyFilter("audit_log_lms");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{AuditLogLms.class, Institution.class};
	}

}
