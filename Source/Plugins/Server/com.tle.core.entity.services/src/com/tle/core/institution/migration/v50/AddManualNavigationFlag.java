package com.tle.core.institution.migration.v50;

import java.io.Serializable;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

/**
 * @author Aaron
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class AddManualNavigationFlag extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(AddManualNavigationFlag.class) + ".";

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 0;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		session.createSQLQuery("UPDATE item SET manual_navigation = ? WHERE manual_navigation IS NULL")
			.setBoolean(0, false).executeUpdate();
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL("item", "manual_navigation");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeItem.class, FakeNavigationSettings.class};
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getAddNotNullSQL("item", "manual_navigation");
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "migration.manualnavigationflag", keyPrefix
			+ "migration.manualnavigationflag");
	}

	@Entity(name = "Item")
	@AccessType("field")
	public static class FakeItem
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Embedded
		FakeNavigationSettings navigationSettings = new FakeNavigationSettings();
	}

	@Embeddable
	@AccessType("field")
	public static class FakeNavigationSettings implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@Column(name = "manualNavigation")
		boolean manualNavigation;
	}
}
