package com.tle.core.migration;

import org.hibernate.Query;
import org.hibernate.classic.Session;

import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.plugins.impl.PluginServiceImpl;

public abstract class AbstractHibernateDataMigration extends AbstractHibernateMigration
{
	private static String prefix = PluginServiceImpl.getMyPluginId(AbstractHibernateDataMigration.class) + "."; //$NON-NLS-1$

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	public void migrate(final MigrationResult result) throws Exception
	{
		result.setupSubTaskStatus(getCalcKey(), 100);
		result.setCanRetry(true);
		final HibernateMigrationHelper helper = createMigrationHelper();

		if( migrationIsRequired(helper) )
		{
			runInTransaction(helper.getFactory(), new HibernateCall()
			{
				@Override
				public void run(Session session) throws Exception
				{
					int dataCount = countDataMigrations(helper, session);
					int totalQueries = dataCount;

					result.setupSubTaskStatus(getCalcKey(), totalQueries);

					result.setupSubSubTask(getDataKey(), dataCount);
					executeDataMigration(helper, result, session);
					session.flush();
				}
			});
		}
	}

	protected boolean migrationIsRequired(HibernateMigrationHelper helper)
	{
		return true;
	}

	protected abstract void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result,
		Session session) throws Exception;

	protected abstract int countDataMigrations(HibernateMigrationHelper helper, Session session);

	@Override
	protected abstract Class<?>[] getDomainClasses();

	protected String getStatusKey()
	{
		return prefix + "migration.sqlstatus"; //$NON-NLS-1$
	}

	protected String getDataKey()
	{
		return prefix + "migration.datastatus"; //$NON-NLS-1$
	}

	protected String getCalcKey()
	{
		return prefix + "migration.datacalc"; //$NON-NLS-1$
	}

	/**
	 * @param session
	 * @param fromAndClause you need to include the FROM
	 * @return
	 */
	protected int count(Session session, String fromAndClause)
	{
		return count(session.createQuery("SELECT COUNT(*) " + fromAndClause)); //$NON-NLS-1$
	}

	protected int count(Query query)
	{
		return ((Number) query.uniqueResult()).intValue();
	}
}
