package com.tle.web.payment.shop.section.cart;

import java.util.List;

import javax.inject.Inject;

import com.tle.common.payment.storefront.entity.Order;
import com.tle.core.payment.storefront.service.OrderService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class ShopOrdersPaymentBox extends AbstractShopOrdersBox
{
	private static final String KEY_MINIMISED = "$SHOP_ORDERS_PAYMENT_MINNED$";

	@PlugKey("shop.orders.payment.title")
	private static Label LABEL_TITLE;

	@Inject
	private OrderService orderService;

	@Override
	protected boolean isShown(SectionInfo info)
	{
		// It will show if there are any orders you are assigned as a payer
		return true;
	}

	@Override
	protected boolean isShowUser(SectionInfo info)
	{
		return true;
	}

	@Override
	protected List<Order> getOrders(SectionInfo info)
	{
		return orderService.enumeratePaymentForUser();
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
