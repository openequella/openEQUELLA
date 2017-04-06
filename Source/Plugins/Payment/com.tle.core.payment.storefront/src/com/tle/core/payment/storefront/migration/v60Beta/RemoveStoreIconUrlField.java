package com.tle.core.payment.storefront.migration.v60Beta;

import java.util.List;

import javax.inject.Singleton;
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

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class RemoveStoreIconUrlField extends AbstractHibernateSchemaMigration
{
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.payment.storefront.migration.removestoreicon");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		result.incrementStatus();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getDropColumnSQL("store", "icon");
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return null;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeStore.class};
	}

	@Entity(name = "Store")
	@AccessType("field")
	class FakeStore
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		String icon;
	}
}
