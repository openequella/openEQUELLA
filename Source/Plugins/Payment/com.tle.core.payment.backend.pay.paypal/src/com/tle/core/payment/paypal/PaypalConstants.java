package com.tle.core.payment.paypal;

import com.tle.core.payment.PaymentGatewayConstants;

@SuppressWarnings("nls")
public class PaypalConstants extends PaymentGatewayConstants
{
	public static final String USERNAME_KEY = "USERNAME_KEY";

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	public PaypalConstants()
	{
		throw new Error();
	}
}
