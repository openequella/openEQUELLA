package com.tle.core.payment.fake;

import com.tle.core.payment.PaymentGatewayConstants;

@SuppressWarnings("nls")
public class FakeGatewayConstants extends PaymentGatewayConstants
{
	public static final String NODELAY_KEY = "NODELAY_KEY";

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	public FakeGatewayConstants()
	{
		throw new Error();
	}
}
