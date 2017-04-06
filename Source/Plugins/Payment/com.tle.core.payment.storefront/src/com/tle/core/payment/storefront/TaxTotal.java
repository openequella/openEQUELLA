package com.tle.core.payment.storefront;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * @author Aaron
 */
public class TaxTotal
{
	private final Currency currency;
	private final String code;
	private final BigDecimal rate;

	private BigDecimal value = BigDecimal.ZERO;

	public TaxTotal(String code, Currency currency, BigDecimal rate)
	{
		this.code = code;
		this.currency = currency;
		this.rate = rate;
	}

	public BigDecimal getValue()
	{
		return value;
	}

	public void incrementValue(BigDecimal modValue)
	{
		value = value.add(modValue);
	}

	public Currency getCurrency()
	{
		return currency;
	}

	public String getCode()
	{
		return code;
	}

	public BigDecimal getRate()
	{
		return rate;
	}
}
