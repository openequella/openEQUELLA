package com.tle.core.activation.convert.migration;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ReIndexActivationsMigration extends AbstractHibernateDataMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(ReIndexActivationsMigration.class) + ".";

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "migration.reindex.activations");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		session
			.createSQLQuery(
				"update item set date_for_index = ? where id in (select ar.item_id from activate_request ar)")
			.setParameter(0, new Date()).executeUpdate();

	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeActivateRequest.class};
	}

	@Entity(name = "ActivateRequest")
	@AccessType("field")
	public static class FakeActivateRequest
	{
		@Id
		private long id;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM ActivateRequest");
	}
}
