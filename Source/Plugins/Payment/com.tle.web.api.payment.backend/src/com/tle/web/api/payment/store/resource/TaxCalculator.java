package com.tle.web.api.payment.store.resource;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import com.google.common.base.Function;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.TaxType;
import com.tle.core.payment.service.TaxService;

/**
 * @author Aaron
 */
public class TaxCalculator implements Function<Price, BigDecimal>
{
	private final TaxService taxService;
	private final List<TaxType> taxes;

	public TaxCalculator(TaxService taxService, List<TaxType> taxes)
	{
		this.taxService = taxService;
		this.taxes = taxes;
	}

	@Override
	public BigDecimal apply(Price price)
	{
		final Currency currency = price.getCurrency();
		final int decimals = (currency == null ? 0 : currency.getDefaultFractionDigits());
		final BigDecimal bigPrice = new BigDecimal(price.getValue()).movePointLeft(decimals);
		return taxService.calculateTax(bigPrice, currency, taxes);
	}
}
