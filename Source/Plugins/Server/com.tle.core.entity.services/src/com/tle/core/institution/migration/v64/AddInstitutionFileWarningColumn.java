package com.tle.core.institution.migration.v64;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AddInstitutionFileWarningColumn extends AbstractHibernateSchemaMigration
{
	private static String prefix = PluginServiceImpl.getMyPluginId(AddInstitutionFileWarningColumn.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(prefix + "migration.v64.AddInstitutionFileWarningColumn.title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		Query q = session.createQuery("UPDATE Institution SET quota = :value");
		q.setParameter("value", 0.0);
		q.executeUpdate();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getAddNotNullSQL("institution", "quota");
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL("institution", "quota");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeInstitution.class};
	}

	@Entity(name = "Institution")
	@AccessType("field")
	public static class FakeInstitution
	{
		@Id
		long id;

		@Column
		double quota;
	}
}
