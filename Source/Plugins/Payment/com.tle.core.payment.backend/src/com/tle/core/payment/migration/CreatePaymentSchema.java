package com.tle.core.payment.migration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import com.google.common.collect.Sets;
import com.tle.beans.security.AccessExpression;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.CatalogueAssignment;
import com.tle.common.payment.entity.PaymentGateway;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.common.payment.entity.Region;
import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.SaleItem;
import com.tle.common.payment.entity.StoreFront;
import com.tle.common.payment.entity.StoreHarvestInfo;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.common.payment.entity.TaxType;
import com.tle.core.dynacollection.DynDependencies;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.ClassDependencies;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.impl.PluginServiceImpl;

@SuppressWarnings("nls")
@Bind
@Singleton
public class CreatePaymentSchema extends AbstractCreateMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(CreatePaymentSchema.class)
		+ ".migration.createentities.";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title");
	}

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		return new TablesOnlyFilter(new String[]{"catalogue", "catalogue_regions", "region", "region_countries",
				"payment_gateway", "payment_gateway_regions", "pricing_tier", "pricing_tier_enabled_periods",
				"pricing_tier_assignment", "catalogue_assignment", "subscription_period", "price", "store_front",
				"sale", "sale_item", "store_harvest_info"});
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		final Set<Class<?>> domainClasses = Sets.newHashSet(ClassDependencies.item());
		domainClasses.addAll(DynDependencies.dynamicCollection());
		// actually, this is implied by Dynamic Collection anyway
		domainClasses.addAll(ClassDependencies.baseEntity());
		domainClasses.add(PaymentGateway.class);
		Collections.addAll(domainClasses, Catalogue.class, CatalogueAssignment.class, Region.class, PricingTier.class,
			SubscriptionPeriod.class, Price.class, PricingTierAssignment.class, StoreFront.class, OAuthClient.class,
			AccessExpression.class, Sale.class, SaleItem.class, StoreHarvestInfo.class, TaxType.class);

		return domainClasses.toArray(new Class<?>[domainClasses.size()]);
	}

	@Override
	protected void addExtraStatements(HibernateMigrationHelper helper, List<String> sql)
	{
		sql.addAll(helper.getAddIndexesRaw("region_countries", "regc_region", "region_id"));
	}
}
