package com.tle.core.harvester.migration.v61;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

/**
 * @author larry
 *
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class AddNewVersionOnHarvestFlag extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(AddNewVersionOnHarvestFlag.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "migration.addnewversiononharvest.title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		session.createQuery("UPDATE HarvesterProfile SET newVersionOnHarvest = ?").setBoolean(0, true).executeUpdate();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM HarvesterProfile");
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		// nothing to drop
		return null;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL("harvester_profile", "new_version_on_harvest");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeBaseEntity.class, FakeHarvesterProfile.class};
	}

	@Entity(name = "BaseEntity")
	@AccessType("field")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static class FakeBaseEntity
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
	}

	@Entity(name = "HarvesterProfile")
	@AccessType("field")
	public static class FakeHarvesterProfile extends FakeBaseEntity
	{
		public boolean newVersionOnHarvest;
	}
}
