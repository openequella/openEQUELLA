package com.tle.web.payment.service;

import com.google.inject.name.Names;
import com.tle.web.payment.section.catalogue.CatalogueContributeSection;
import com.tle.web.payment.section.catalogue.RootCatalogueSection;
import com.tle.web.payment.section.catalogue.ShowCataloguesSection;
import com.tle.web.payment.section.gateway.GatewayContributeSection;
import com.tle.web.payment.section.gateway.RootGatewaySection;
import com.tle.web.payment.section.gateway.ShowGatewaysSection;
import com.tle.web.payment.section.region.RegionContributeSection;
import com.tle.web.payment.section.region.RootRegionSection;
import com.tle.web.payment.section.region.ShowRegionsSection;
import com.tle.web.payment.section.store.StoreSettingsSection;
import com.tle.web.payment.section.storefront.RootStoreFrontSection;
import com.tle.web.payment.section.storefront.ShowStoreFrontsSection;
import com.tle.web.payment.section.storefront.StoreFrontContributeSection;
import com.tle.web.payment.section.tax.RootTaxSection;
import com.tle.web.payment.section.tax.ShowTaxesSection;
import com.tle.web.payment.section.tax.TaxContributeSection;
import com.tle.web.payment.section.tier.RootTierSection;
import com.tle.web.payment.section.tier.ShowPurchaseTiersSection;
import com.tle.web.payment.section.tier.ShowSubscriptionTiersSection;
import com.tle.web.payment.section.tier.TierContributeSection;
import com.tle.web.payment.section.tier.TierOptionsSection;
import com.tle.web.sections.equella.guice.SectionsModule;

/**
 * @author Aaron
 */

@SuppressWarnings("nls")
public class PaymentModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("catalogueTree")).toProvider(catalogueTree());

		bind(Object.class).annotatedWith(Names.named("gatewayTree")).toProvider(gatewayTree());

		bind(Object.class).annotatedWith(Names.named("regionTree")).toProvider(regionTree());

		bind(Object.class).annotatedWith(Names.named("tierTree")).toProvider(tierTree());

		bind(Object.class).annotatedWith(Names.named("storefrontTree")).toProvider(storefrontTree());

		bind(Object.class).annotatedWith(Names.named("taxTree")).toProvider(taxTree());

		bind(Object.class).annotatedWith(Names.named("/access/storesettings")).toProvider(
			node(StoreSettingsSection.class));
	}

	private NodeProvider catalogueTree()
	{
		NodeProvider node = node(RootCatalogueSection.class);
		node.innerChild(CatalogueContributeSection.class);
		node.child(ShowCataloguesSection.class);
		return node;
	}

	private NodeProvider gatewayTree()
	{
		NodeProvider node = node(RootGatewaySection.class);
		node.innerChild(GatewayContributeSection.class);
		node.child(ShowGatewaysSection.class);
		return node;
	}

	private NodeProvider regionTree()
	{
		NodeProvider node = node(RootRegionSection.class);
		node.innerChild(RegionContributeSection.class);
		node.child(ShowRegionsSection.class);
		return node;
	}

	private NodeProvider tierTree()
	{
		NodeProvider root = node(RootTierSection.class);
		root.innerChild(TierContributeSection.class);

		NodeProvider tiers = node(TierOptionsSection.class);
		tiers.child(ShowPurchaseTiersSection.class);
		tiers.child(ShowSubscriptionTiersSection.class);
		root.child(tiers);

		return root;
	}

	private NodeProvider storefrontTree()
	{
		NodeProvider node = node(RootStoreFrontSection.class);
		node.innerChild(StoreFrontContributeSection.class);
		node.child(ShowStoreFrontsSection.class);
		return node;
	}

	private NodeProvider taxTree()
	{
		NodeProvider node = node(RootTaxSection.class);
		node.innerChild(TaxContributeSection.class);
		node.child(ShowTaxesSection.class);
		return node;
	}
}
