package com.tle.core.institution.migration.v52;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class RemoveUnusedSystemSettingsPrivilegesDatabaseMigration extends AbstractHibernateDataMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(RemoveUnusedSystemSettingsPrivilegesDatabaseMigration.class) + ".";

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "migration.unusedsystemsettingsprivs");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		session.createQuery(
			"DELETE FROM AccessEntry a WHERE a.targetObject IN ('C:attachmentFileTypes', 'C:proxy', 'C:sif')") //$NON-NLS-1$
			.executeUpdate();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		int ct = count(session
			.createQuery("SELECT COUNT(*) FROM AccessEntry a WHERE a.targetObject IN ('C:attachmentFileTypes', 'C:proxy', 'C:sif')"));
		return ct;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeAccessEntry.class};
	}

	@Entity(name = "AccessEntry")
	@AccessType("field")
	public static class FakeAccessEntry
	{
		@Id
		long id;

		String targetObject;
	}
}
