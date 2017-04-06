package com.tle.tomcat.migration;

import java.util.List;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.services.EventService;
import com.tle.tomcat.events.TomcatRestartEvent;

@Bind
@Singleton
@SuppressWarnings("nls")
public class CreateTomcatSessionEntity extends AbstractCreateMigration
{
	@Inject
	private EventService eventService;
	
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("migration.create.info");
	}
	
	@Override
	public void migrate(MigrationResult result) throws Exception
	{
		super.migrate(result);
		eventService.publishApplicationEvent(new TomcatRestartEvent());
	}

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		return new TablesOnlyFilter("tomcat_sessions");
	}

	@Override
	protected void addExtraStatements(HibernateMigrationHelper helper, List<String> statements)
	{
		helper.getAddIndexesForColumns("tomcat_sessions", "app");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{TomcatSessions.class};
	}
}
