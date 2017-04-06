package com.tle.core.institution.migration.v62;

import java.util.Date;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.google.common.collect.Lists;
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
public class AddItemDateForIndexIndex extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(AddItemDateForIndexIndex.class)
		+ ".migration.v62.dateforindexindex.";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// No data migration
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 0;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> sql = Lists.newArrayList();
		sql.addAll(helper.getAddNotNullSQL("item", "date_for_index"));
		sql.addAll(helper.getAddIndexesRaw("item", "itemDateForIndexIndex", "date_for_index"));
		return sql;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return null;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeItem.class};
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Entity(name = "Item")
	@AccessType("field")
	public static class FakeItem
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Index(name = "itemDateForIndexIndex")
		@Column(nullable = false)
		Date dateForIndex;
	}
}
