package com.tle.core.payment.migration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.classic.Session;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.Region;
import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.SaleItem;
import com.tle.common.payment.entity.StoreFront;
import com.tle.common.payment.entity.StoreHarvestInfo;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.common.payment.entity.TaxType;
import com.tle.core.dynacollection.DynDependencies;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.ClassDependencies;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AddForeignIndexes extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(AddForeignIndexes.class) + ".migration."; //$NON-NLS-1$

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 0;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = Lists.newArrayList();
		sql.addAll(helper.getAddIndexesForColumns("catalogue", "dynamic_collection_id"));
		sql.addAll(helper.getAddIndexesForColumns("price", "period_id"));
		sql.addAll(helper.getAddIndexesForColumns("sale_item", "period_id"));
		sql.addAll(helper.getAddIndexesForColumns("store_harvest_info", "sale_id"));
		return sql;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		final Set<Class<?>> domainClasses = Sets.newHashSet(ClassDependencies.item());
		domainClasses.addAll(DynDependencies.dynamicCollection());
		Collections.addAll(domainClasses, new Class<?>[]{Catalogue.class, Price.class, SaleItem.class,
				StoreHarvestInfo.class, Sale.class, StoreFront.class, SubscriptionPeriod.class, Region.class,
				PricingTier.class, OAuthClient.class, TaxType.class});
		return domainClasses.toArray(new Class<?>[domainClasses.size()]);
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "addfkeys");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// nothing
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return null;
	}

}
