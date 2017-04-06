package com.tle.web.payment.shop.guice;

import com.google.inject.name.Names;
import com.tle.web.payment.shop.section.CartColumnSection;
import com.tle.web.payment.shop.section.RootShopSection;
import com.tle.web.payment.shop.section.ShopCataloguesSection;
import com.tle.web.payment.shop.section.ShopStoresSection;
import com.tle.web.payment.shop.section.cart.ShopCartBoxSection;
import com.tle.web.payment.shop.section.cart.ShopOrdersApprovalBox;
import com.tle.web.payment.shop.section.cart.ShopOrdersPaymentBox;
import com.tle.web.payment.shop.section.cart.ShopOrdersSentBox;
import com.tle.web.payment.shop.section.cart.ShopViewCartSection;
import com.tle.web.payment.shop.section.viewitem.RootShopViewItemSection;
import com.tle.web.payment.shop.section.viewitem.ShopItemPurchaseDetailsSection;
import com.tle.web.payment.shop.section.viewitem.ShopViewItemSection;
import com.tle.web.sections.equella.guice.SectionsModule;

/**
 * Shop only sections
 * 
 * @author Aaron
 */
public class ShopModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("shopStoresTree")).toProvider(shopStoresTree());

		bind(Object.class).annotatedWith(Names.named("shopStoreTree")).toProvider(shopStoreTree());

		bind(Object.class).annotatedWith(Names.named("shopViewItemTree")).toProvider(shopViewItemTree());

		bind(Object.class).annotatedWith(Names.named("shopCartTree")).toProvider(shopCartTree());
	}

	private NodeProvider shopStoresTree()
	{
		NodeProvider node = node(RootShopSection.class);
		node.child(ShopStoresSection.class);
		node.child(actions(true));
		return node;
	}

	private NodeProvider shopStoreTree()
	{
		NodeProvider node = node(RootShopSection.class);
		node.child(ShopCataloguesSection.class);
		node.child(actions(false));
		return node;
	}

	private NodeProvider shopViewItemTree()
	{
		NodeProvider node = node(RootShopViewItemSection.class);
		node.child(ShopViewItemSection.class);
		node.child(ShopItemPurchaseDetailsSection.class);
		node.child(actions(false));
		return node;
	}

	private NodeProvider shopCartTree()
	{
		return node(ShopViewCartSection.class);
	}

	private NodeProvider actions(boolean orders)
	{
		NodeProvider actions = node(CartColumnSection.class);
		actions.child(ShopCartBoxSection.class);
		if( orders )
		{
			actions.child(ShopOrdersSentBox.class);
			actions.child(ShopOrdersApprovalBox.class);
			actions.child(ShopOrdersPaymentBox.class);
		}
		return actions;
	}
}
