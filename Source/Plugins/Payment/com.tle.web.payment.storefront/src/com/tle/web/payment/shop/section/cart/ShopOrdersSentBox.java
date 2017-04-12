package com.tle.web.payment.shop.section.cart;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.tle.common.payment.storefront.entity.Order;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.payment.storefront.service.OrderService;
import com.tle.core.security.TLEAclManager;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

/**
 * @author Aaron
 */
public class ShopOrdersSentBox extends AbstractShopOrdersBox
{
	private static final String KEY_MINIMISED = "$SHOP_ORDERS_SENT_MINNED$"; //$NON-NLS-1$

	@PlugKey("shop.orders.sent.title")
	private static Label LABEL_TITLE;

	@Inject
	private OrderService orderService;
	@Inject
	private TLEAclManager aclService;

	@Override
	protected boolean isShown(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(
			Collections.singleton(StoreFrontConstants.PRIV_ACCESS_SHOPPING_CART)).isEmpty();
	}

	@Override
	protected boolean isShowUser(SectionInfo info)
	{
		return false;
	}

	@Override
	protected List<Order> getOrders(SectionInfo info)
	{
		return orderService.enumerateSent();
	}

	@Override
	protected Label getBoxTitle()
	{
		return LABEL_TITLE;
	}

	@Override
	protected String getMinimisedKey()
	{
		return KEY_MINIMISED;
	}
}
