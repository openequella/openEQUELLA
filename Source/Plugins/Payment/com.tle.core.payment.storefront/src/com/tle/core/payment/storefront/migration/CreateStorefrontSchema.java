package com.tle.core.payment.storefront.migration;

import java.util.Set;

import javax.inject.Singleton;

import com.google.common.collect.Sets;
import com.tle.common.payment.storefront.entity.Order;
import com.tle.common.payment.storefront.entity.OrderHistory;
import com.tle.common.payment.storefront.entity.OrderItem;
import com.tle.common.payment.storefront.entity.OrderStorePart;
import com.tle.common.payment.storefront.entity.Purchase;
import com.tle.common.payment.storefront.entity.PurchaseItem;
import com.tle.common.payment.storefront.entity.PurchasedContent;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.ClassDependencies;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class CreateStorefrontSchema extends AbstractCreateMigration
{
	private static final String TITLE_KEY = PluginServiceImpl.getMyPluginId(CreateStorefrontSchema.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(TITLE_KEY + "migration.create.entities");
	}

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		return new TablesOnlyFilter(new String[]{"store", "purchased_content", "purchase", "purchase_item", "order",
				"order_store_part", "order_item", "order_history"});
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		final Set<Class<?>> domainClasses = Sets.newHashSet(ClassDependencies.item());
		domainClasses.addAll(ClassDependencies.baseEntity());
		domainClasses.add(Store.class);
		domainClasses.add(Order.class);
		domainClasses.add(OrderStorePart.class);
		domainClasses.add(OrderItem.class);
		domainClasses.add(OrderHistory.class);
		domainClasses.add(Purchase.class);
		domainClasses.add(PurchaseItem.class);
		domainClasses.add(PurchasedContent.class);
		return domainClasses.toArray(new Class<?>[domainClasses.size()]);
	}

}
