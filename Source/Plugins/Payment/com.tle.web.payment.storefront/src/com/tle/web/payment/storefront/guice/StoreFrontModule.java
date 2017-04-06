package com.tle.web.payment.storefront.guice;

import com.google.inject.name.Names;
import com.tle.web.payment.storefront.section.settings.StoreFrontSettingsSection;
import com.tle.web.payment.storefront.section.store.RootStoreSection;
import com.tle.web.payment.storefront.section.store.ShowStoresSection;
import com.tle.web.payment.storefront.section.store.StoreContributeSection;
import com.tle.web.payment.storefront.section.store.StoreRegisterSection;
import com.tle.web.payment.storefront.section.store.ViewStoreInfoSection;
import com.tle.web.payment.storefront.section.workflow.EditApprovalsSection;
import com.tle.web.payment.storefront.section.workflow.RootApprovalsSection;
import com.tle.web.payment.storefront.section.workflow.ShowApprovalsSection;
import com.tle.web.sections.equella.guice.SectionsModule;

/**
 * Store front admin sections
 */
@SuppressWarnings("nls")
public class StoreFrontModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("storeTree")).toProvider(storeListTree());

		bind(Object.class).annotatedWith(Names.named("registerStoreTree")).toProvider(registerStoreTree());

		bind(Object.class).annotatedWith(Names.named("/access/storefrontsettings")).toProvider(
			node(StoreFrontSettingsSection.class));

		bind(Object.class).annotatedWith(Names.named("/access/approvals")).toProvider(approvalsTree());
	}

	private NodeProvider storeListTree()
	{
		NodeProvider node = node(RootStoreSection.class);
		node.child(ShowStoresSection.class);
		node.innerChild(StoreContributeSection.class);
		node.innerChild(ViewStoreInfoSection.class);
		return node;
	}

	private NodeProvider registerStoreTree()
	{
		return node(StoreRegisterSection.class);
	}

	private NodeProvider approvalsTree()
	{
		NodeProvider node = node(RootApprovalsSection.class);
		node.innerChild(EditApprovalsSection.class);
		node.child(ShowApprovalsSection.class);
		return node;
	}
}
