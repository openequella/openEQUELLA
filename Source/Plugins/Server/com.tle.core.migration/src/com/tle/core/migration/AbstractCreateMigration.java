package com.tle.core.migration;

import java.util.List;

import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;

public abstract class AbstractCreateMigration extends AbstractHibernateMigration
{
	public static final String KEY_STATUS = "com.tle.core.migration.migration.sqlstatus"; //$NON-NLS-1$
	public static final String KEY_CALCULATING = "com.tle.core.migration.migration.sqlcalc"; //$NON-NLS-1$

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	public void migrate(MigrationResult result) throws Exception
	{
		result.setupSubTaskStatus(getCalcKey(), 100);
		result.setCanRetry(true);
		HibernateMigrationHelper helper = createMigrationHelper();
		List<String> createStatements = helper.getCreationSql(getFilter(helper));
		addExtraStatements(helper, createStatements);
		runSqlStatements(createStatements, helper.getFactory(), result, getStatusKey());
	}

	protected void addExtraStatements(HibernateMigrationHelper helper, List<String> statements)
	{
		// nothing
	}

	protected String getStatusKey()
	{
		return KEY_STATUS;
	}

	protected String getCalcKey()
	{
		return KEY_CALCULATING;
	}

	protected abstract HibernateCreationFilter getFilter(HibernateMigrationHelper helper);
}
