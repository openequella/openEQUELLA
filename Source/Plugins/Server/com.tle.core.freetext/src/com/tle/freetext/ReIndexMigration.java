package com.tle.freetext;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.CurrentDataSource;
import com.tle.core.hibernate.HibernateFactoryService;
import com.tle.core.migration.AbstractHibernateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class ReIndexMigration extends AbstractHibernateMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(ReIndexMigration.class) + "."; //$NON-NLS-1$

	@Inject
	private HibernateFactoryService hibernateService;

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{};
	}

	@SuppressWarnings("nls")
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "reindex.title", KEY_PREFIX + "reindex.desc");
	}

	@Override
	public void migrate(MigrationResult status) throws Exception
	{
		runInTransaction(hibernateService.createConfiguration(CurrentDataSource.get(), getDomainClasses())
			.getSessionFactory(), new HibernateCall()
		{
			@SuppressWarnings("nls")
			@Override
			public void run(Session session) throws Exception
			{
				session.createSQLQuery("update item set date_for_index = ?").setParameter(0, new Date())
					.executeUpdate();

			}
		});
	}
}
