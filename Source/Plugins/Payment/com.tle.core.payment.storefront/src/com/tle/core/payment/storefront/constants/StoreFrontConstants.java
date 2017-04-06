package com.tle.core.payment.storefront.constants;

@SuppressWarnings("nls")
public final class StoreFrontConstants
{
	private StoreFrontConstants()
	{
		throw new Error();
	}

	public static final String PRIV_CREATE_STORE = "CREATE_STORE";
	public static final String PRIV_EDIT_STORE = "EDIT_STORE";
	public static final String PRIV_DELETE_STORE = "DELETE_STORE";
	// Used for showing the stores list for shopping
	public static final String PRIV_BROWSE_STORE = "BROWSE_STORE";
	public static final String PRIV_ACCESS_SHOPPING_CART = "ACCESS_SHOPPING_CART";

	public static final String PRIV_VIEW_PURCHASE_DETAILS_FOR_ITEM = "VIEW_PURCHASE_DETAILS_FOR_ITEM";

	public static final String NOTIFICATION_REASON_REQUIRES_APPROVAL = "apprvodr";
	public static final String NOTIFICATION_REASON_REQUIRES_PAYMENT = "payorder";
}
