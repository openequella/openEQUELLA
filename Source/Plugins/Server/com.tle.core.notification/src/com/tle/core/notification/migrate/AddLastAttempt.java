package com.tle.core.notification.migrate;

import java.util.Date;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.common.i18n.KeyString;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@SuppressWarnings("nls")
@Bind
@Singleton
public class AddLastAttempt extends AbstractHibernateSchemaMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(AddLastAttempt.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(new KeyString(KEY_PREFIX + "migrate.addlastattempt"));
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// nothing
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 0;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getAddIndexesAndConstraintsForColumns("notification", "last_attempt", "attempt_id");
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL("notification", "last_attempt", "attempt_id");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeNotification.class};
	}

	@Entity(name = "Notification")
	public static class FakeNotification
	{
		@Id
		long id;

		@Column
		@Index(name = "lastattempt_idx")
		Date lastAttempt;

		@Column(length = 36)
		@Index(name = "attempt_idx")
		String attemptId;
	}

}
