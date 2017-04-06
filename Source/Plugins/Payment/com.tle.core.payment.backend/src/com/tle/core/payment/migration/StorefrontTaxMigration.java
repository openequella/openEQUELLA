package com.tle.core.payment.migration;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class StorefrontTaxMigration extends AbstractHibernateSchemaMigration
{
	private static final String TABLE_STOREFRONT = "store_front";
	private static final String COL_TAXTYPE = "tax_type_id";

	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(StorefrontTaxMigration.class)
		+ ".migration.storefronttaxtype.";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Just adding the column
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL(TABLE_STOREFRONT, COL_TAXTYPE);
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeStorefront.class, FakeTaxType.class};
	}

	@Entity(name = "StoreFront")
	@AccessType("field")
	public static class FakeStorefront
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "taxTypeIndex")
		FakeTaxType taxType;
	}

	@Entity(name = "TaxType")
	@AccessType("field")
	public static class FakeTaxType
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
	}
}
