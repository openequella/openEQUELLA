package com.tle.web.payment.shop.guice;

import com.tle.web.payment.shop.section.CartColumnSection;
import com.tle.web.payment.shop.section.cart.ShopCartBoxSection;
import com.tle.web.payment.shop.section.cart.ShopOrdersApprovalBox;
import com.tle.web.payment.shop.section.cart.ShopOrdersPaymentBox;
import com.tle.web.payment.shop.section.cart.ShopOrdersSentBox;
import com.tle.web.payment.shop.section.search.RootShopSearchSection;
import com.tle.web.payment.shop.section.search.ShopSearchFilterByDateRangeSection;
import com.tle.web.payment.shop.section.search.ShopSearchFilterByPricingModelSection;
import com.tle.web.payment.shop.section.search.ShopSearchInfoSection;
import com.tle.web.payment.shop.section.search.ShopSearchResultsSection;
import com.tle.web.payment.shop.section.search.ShopSearchSortSection;
import com.tle.web.search.filter.SimpleResetFiltersQuerySection;
import com.tle.web.search.guice.AbstractSearchModule;

@SuppressWarnings("nls")
public class ShopSearchModule extends AbstractSearchModule
{
	@Override
	protected NodeProvider getRootNode()
	{
		NodeProvider node = node(RootShopSearchSection.class);
		node.child(ShopSearchInfoSection.class);
		return node;
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(SimpleResetFiltersQuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(ShopSearchResultsSection.class);
	}

	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(ShopSearchSortSection.class);
		node.child(ShopSearchFilterByDateRangeSection.class);
		node.child(ShopSearchFilterByPricingModelSection.class);
	}

	@Override
	protected void addActions(NodeProvider node)
	{
		node.child(shopActions(false));
	}

	private NodeProvider shopActions(boolean orders)
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

	@Override
	protected String getTreeName()
	{
		return "shopSearchTree";
	}
}
