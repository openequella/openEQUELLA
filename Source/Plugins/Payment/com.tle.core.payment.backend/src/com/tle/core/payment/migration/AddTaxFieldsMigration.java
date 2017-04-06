package com.tle.core.payment.migration;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Min;

import org.hibernate.annotations.AccessType;
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
public class AddTaxFieldsMigration extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(AddTaxFieldsMigration.class)
		+ ".migration.addtaxfields.";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		session.createQuery("UPDATE Sale SET tax = 0").executeUpdate();
		result.incrementStatus();
		session.createQuery("UPDATE SaleItem SET tax = 0, unitTax = 0, unitPrice = 0").executeUpdate();
		result.incrementStatus();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 2;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> sql = helper.getAddNotNullSQL("sale", "tax");
		sql.addAll(helper.getAddNotNullSQL("sale_item", "tax"));
		sql.addAll(helper.getAddNotNullSQL("sale_item", "unit_tax"));
		sql.addAll(helper.getAddNotNullSQL("sale_item", "unit_price"));
		return sql;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = helper.getAddColumnsSQL("sale", "tax");
		sql.addAll(helper.getAddColumnsSQL("sale", "tax_code"));
		sql.addAll(helper.getAddColumnsSQL("sale", "tax_percent"));
		sql.addAll(helper.getAddColumnsSQL("sale_item", "tax"));
		sql.addAll(helper.getAddColumnsSQL("sale_item", "tax_code"));
		sql.addAll(helper.getAddColumnsSQL("sale_item", "unit_price"));
		sql.addAll(helper.getAddColumnsSQL("sale_item", "unit_tax"));
		return sql;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeSale.class, FakeSaleItem.class};
	}

	@Entity(name = "Sale")
	@AccessType("field")
	public static class FakeSale
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Column(nullable = false)
		@Min(0)
		long tax;

		@Column(length = 10, nullable = true)
		String taxCode;

		@Column(precision = 9, scale = 4)
		@Min(0)
		BigDecimal taxPercent;
	}

	@Entity(name = "SaleItem")
	@AccessType("field")
	public static class FakeSaleItem
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Column(nullable = false)
		@Min(0)
		long tax;

		@Column(length = 10, nullable = true)
		String taxCode;

		@Column(nullable = false)
		@Min(0)
		long unitPrice;

		@Column(nullable = false)
		@Min(0)
		long unitTax;
	}
}
