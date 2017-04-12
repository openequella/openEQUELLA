package com.tle.core.qti.migration;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Index;
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
@SuppressWarnings("nls")
@Bind
@Singleton
public class AddItemVariableValueIndex extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(AddItemVariableValueIndex.class)
		+ ".migration.additemvariablevalueindex.";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// None
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 0;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return null;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddIndexesAndConstraintsForColumns("qti_item_variable_value", "qti_item_variable_id");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeItemVariableValue.class};
	}

	@Entity(name = "QtiItemVariableValue")
	public static class FakeItemVariableValue
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Index(name = "itemValVarIdx")
		long qtiItemVariableId;
	}
}
