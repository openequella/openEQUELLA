package com.tle.core.payment;

@SuppressWarnings("nls")
public final class PaymentIndexFields
{
	private PaymentIndexFields()
	{
		throw new Error();
	}

	public static final String FIELD_PURCHASE_TIER = "purchasetier";
	public static final String FIELD_SUBSCRIPTION_TIER = "subscriptiontier";
	public static final String FIELD_FREE_TIER = "freetier";

	public static final String FIELD_BLACKLISTED = "blacklisted_catalogue";
	public static final String FIELD_WHITELISTED = "whitelisted_catalogue";
}
