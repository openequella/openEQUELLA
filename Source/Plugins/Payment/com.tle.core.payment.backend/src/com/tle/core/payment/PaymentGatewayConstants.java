package com.tle.core.payment;

@SuppressWarnings("nls")
public class PaymentGatewayConstants
{
	// Sonar likes unreachable constructors for non-instantiated utility
	// classes, but this one has non-instantiated descendants
	protected PaymentGatewayConstants()
	{
		throw new Error();
	}

	public static final String SANDBOX_KEY = "SANDBOX_KEY";
	public static final String FIELD_TESTED = "FIELD_TESTED";
}
