package com.tle.core.payment;

@SuppressWarnings("nls")
public final class PaymentConstants
{
	private PaymentConstants()
	{
		throw new Error();
	}

	public static final String PRIV_CREATE_REGION = "CREATE_REGION";
	public static final String PRIV_EDIT_REGION = "EDIT_REGION";
	public static final String PRIV_DELETE_REGION = "DELETE_REGION";

	public static final String PRIV_CREATE_CATALOGUE = "CREATE_CATALOGUE";
	public static final String PRIV_EDIT_CATALOGUE = "EDIT_CATALOGUE";
	public static final String PRIV_DELETE_CATALOGUE = "DELETE_CATALOGUE";
	public static final String PRIV_MANAGE_CATALOGUE = "MANAGE_CATALOGUE";

	public static final String PRIV_CREATE_TIER = "CREATE_TIER";
	public static final String PRIV_EDIT_TIER = "EDIT_TIER";
	public static final String PRIV_DELETE_TIER = "DELETE_TIER";

	// public static final String PRIV_CREATE_STOREFRONT = "CREATE_STOREFRONT";
	public static final String PRIV_EDIT_STOREFRONT = "EDIT_STOREFRONT";
	public static final String PRIV_DELETE_STOREFRONT = "DELETE_STOREFRONT";

	public static final String PRIV_CREATE_TAX = "CREATE_TAX";
	public static final String PRIV_EDIT_TAX = "EDIT_TAX";
	public static final String PRIV_DELETE_TAX = "DELETE_TAX";

	public static final String PRIV_CREATE_PAYMENT_GATEWAY = "CREATE_PAYMENT_GATEWAY";
	public static final String PRIV_EDIT_PAYMENT_GATEWAY = "EDIT_PAYMENT_GATEWAY";
	public static final String PRIV_DELETE_PAYMENT_GATEWAY = "DELETE_PAYMENT_GATEWAY";

	public static final String PRIV_SET_TIERS_FOR_ITEM = "SET_TIERS_FOR_ITEM";
	public static final String PRIV_VIEW_SALES_FOR_ITEM = "VIEW_SALES_FOR_ITEM";
	public static final String PRIV_VIEW_TIERS_FOR_ITEM = "VIEW_TIERS_FOR_ITEM";

	public static final String NOTIFICATION_REASON_ITEMSALE = "itemsale";

	public static final String LICENSE_FEATURE_CONTENT_EXCHANGE = "contentExchange";
}
