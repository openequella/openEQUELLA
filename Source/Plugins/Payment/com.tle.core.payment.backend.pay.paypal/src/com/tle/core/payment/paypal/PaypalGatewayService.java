package com.tle.core.payment.paypal;

import java.util.Currency;

public interface PaypalGatewayService
{
	boolean isSupportedCurrency(Currency currency);
}
