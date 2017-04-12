package com.tle.core.payment.storefront.migration;

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
		session.createQuery("UPDATE OrderStorePart SET tax = 0").executeUpdate();
		result.incrementStatus();
		session.createQuery("UPDATE OrderItem SET tax = 0, unitTax = 0").executeUpdate();
		result.incrementStatus();
		session.createQuery("UPDATE Purchase SET tax = 0").executeUpdate();
		result.incrementStatus();
		session.createQuery("UPDATE PurchaseItem SET tax = 0").executeUpdate();
		result.incrementStatus();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 4;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> sql = helper.getAddNotNullSQL("order_item", "tax");
		sql.addAll(helper.getAddNotNullSQL("order_item", "unit_tax"));
		sql.addAll(helper.getAddNotNullSQL("order_store_part", "tax"));
		sql.addAll(helper.getAddNotNullSQL("purchase", "tax"));
		sql.addAll(helper.getAddNotNullSQL("purchase_item", "tax"));
		return sql;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = helper.getAddColumnsSQL("order_item", "tax");
		sql.addAll(helper.getAddColumnsSQL("order_item", "unit_tax"));
		sql.addAll(helper.getAddColumnsSQL("order_item", "tax_code"));
		sql.addAll(helper.getAddColumnsSQL("order_store_part", "tax"));
		sql.addAll(helper.getAddColumnsSQL("order_store_part", "tax_code"));
		sql.addAll(helper.getAddColumnsSQL("purchase", "tax"));
		sql.addAll(helper.getAddColumnsSQL("purchase", "tax_code"));
		sql.addAll(helper.getAddColumnsSQL("purchase_item", "tax"));
		sql.addAll(helper.getAddColumnsSQL("purchase_item", "tax_code"));
		return sql;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeOrderItem.class, FakeOrderStorePart.class, FakePurchase.class, FakePurchaseItem.class};
	}

	@Entity(name = "OrderItem")
	@AccessType("field")
	public static class FakeOrderItem
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
		long unitTax;
	}

	@Entity(name = "OrderStorePart")
	@AccessType("field")
	public static class FakeOrderStorePart
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Column(nullable = false)
		@Min(0)
		long tax;

		@Column(length = 10, nullable = true)
		String taxCode;
	}

	@Entity(name = "Purchase")
	@AccessType("field")
	public static class FakePurchase
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Column(nullable = false)
		@Min(0)
		long tax;

		@Column(length = 10, nullable = true)
		String taxCode;
	}

	@Entity(name = "PurchaseItem")
	@AccessType("field")
	public static class FakePurchaseItem
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Column(nullable = false)
		@Min(0)
		long tax;

		@Column(length = 10, nullable = true)
		String taxCode;
	}
}
