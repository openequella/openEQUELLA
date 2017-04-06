package com.tle.core.echo.migration;

import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.core.echo.entity.EchoServer;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;

@SuppressWarnings("nls")
@Bind
@Singleton
public class CreateEchoEntities extends AbstractCreateMigration
{
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.echo.migration.info.create");
	}

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		return new TablesOnlyFilter("echo_server");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{EchoServer.class, BaseEntity.class, BaseEntity.Attribute.class, LanguageBundle.class,
				Institution.class, LanguageString.class};
	}
}
