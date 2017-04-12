package com.tle.core.payment.storefront;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * @author Aaron
 */
public class CurrencyTotal
{
	private final Currency currency;
	private BigDecimal value = BigDecimal.ZERO;
	private final Map<String, TaxTotal> taxes = Maps.newHashMap();

	public CurrencyTotal(Currency currency)
	{
		this.currency = currency;
	}

	public BigDecimal getValue()
	{
		return value;
	}

	public void incrementValue(BigDecimal modValue)
	{
		value = value.add(modValue);
	}

	public synchronized TaxTotal getTaxTotal(String code)
	{
		if( code == null )
		{
			return null;
		}

		TaxTotal tax = taxes.get(code);
		if( tax == null )
		{
			tax = new TaxTotal(code, currency, null);
			taxes.put(code, tax);
		}
		return tax;
	}

	public BigDecimal getCombinedTaxTotal()
	{
		BigDecimal combs = BigDecimal.ZERO;
		for( TaxTotal tax : getTaxes() )
		{
			combs = combs.add(tax.getValue());
		}
		return combs;
	}

	public Collection<TaxTotal> getTaxes()
	{
		return taxes.values();
	}

	public Currency getCurrency()
	{
		return currency;
	}
}
