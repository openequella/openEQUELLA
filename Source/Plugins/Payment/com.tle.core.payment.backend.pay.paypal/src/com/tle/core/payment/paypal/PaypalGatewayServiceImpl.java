package com.tle.core.payment.paypal;

import java.util.Currency;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.core.guice.Bindings;
import com.tle.core.payment.gateway.PaymentGatewayImplementation;
import com.tle.core.payment.service.session.PaymentGatewayEditingBean;

@Bindings({@Bind(PaypalGatewayService.class), @Bind(PaymentGatewayImplementation.class)})
@Singleton
@SuppressWarnings({"nls"})
public class PaypalGatewayServiceImpl implements PaypalGatewayService, PaymentGatewayImplementation
{
	private static Set<String> SUPPORTED_CURRENCIES = Sets.newHashSet("AUD", "BRL", "CAD", "CZK", "DKK", "EUR", "HKD",
		"HUF", "ILS", "JPY", "MYR", "MXN", "NOK", "NZD", "PHP", "PLN", "GBP", "SGD", "SEK", "CHF", "TWD", "THB", "TRY",
		"USD");

	@Override
	public String testCredentials(PaymentGatewayEditingBean gateway)
	{
		throw new Error("Not supported");
	}

	@Override
	public boolean isSupportedCurrency(Currency currency)
	{
		return SUPPORTED_CURRENCIES.contains(currency.getCurrencyCode());
	}
}
