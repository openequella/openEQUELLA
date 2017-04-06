package com.tle.web.payment.shop;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public final class ShopConstants
{
	public static final String URL_SHOPS = "/access/shop/shop.do";
	public static final String URL_STORE = "/access/shop/store.do";
	public static final String URL_SEARCH = "/access/shop/search.do";
	public static final String URL_VIEWITEM = "/access/shop/viewitem.do";
	public static final String URL_CART = "/access/shop/cart.do";
	public static final String URL_PAY_RETURN = "/access/shop/payreturn.do";

	public static final String KEY_PRICE_SELECTIONS = "shop.priceselections";

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	private ShopConstants()
	{
		throw new Error();
	}
}
