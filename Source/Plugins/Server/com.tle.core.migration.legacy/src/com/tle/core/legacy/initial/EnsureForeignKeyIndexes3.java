package com.tle.core.legacy.initial;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.classic.Session;

import com.tle.beans.Institution;
import com.tle.beans.user.TLEGroup;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.AbstractHibernateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class EnsureForeignKeyIndexes3 extends AbstractHibernateMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(EnsureForeignKeyIndexes3.class) + ".ensurefki3.";

	@Override
	public void migrate(MigrationResult status) throws Exception
	{
		status.setupSubTaskStatus(AbstractCreateMigration.KEY_CALCULATING, 100);
		status.setCanRetry(true);
		HibernateMigrationHelper helper = createMigrationHelper();
		List<String> sql = new ArrayList<String>();
		Session session = helper.getFactory().openSession();

		sql.addAll(helper.getAddIndexesRaw("tlegroup_users", "tleguElem", "element"));

		session.close();
		runSqlStatements(sql, helper.getFactory(), status, AbstractCreateMigration.KEY_STATUS);
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{Institution.class, TLEGroup.class,};
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title", keyPrefix + "description");
	}
}
