package com.tle.web.sections.result.util;

import java.math.BigDecimal;
import java.util.Currency;

import com.tle.web.sections.result.util.CurrencyLabel.CurrencyInfo;

public class MoneyLabel extends NumberLabel
{
	private static final long serialVersionUID = 1L;

	private final Currency currency;
	private final boolean showSymbol;
	private final boolean showCode;
	private final boolean html;

	public MoneyLabel(long value, Currency currency)
	{
		this(value, currency, true, true, false);
	}

	public MoneyLabel(BigDecimal value, Currency currency)
	{
		this(value, currency, true, true, false);
	}

	/**
	 * @param value
	 * @param currency
	 * @param html Do not entity encode the text
	 */
	public MoneyLabel(long value, Currency currency, boolean html)
	{
		this(value, currency, true, true, html);
	}

	public MoneyLabel(BigDecimal value, Currency currency, boolean showSymbol, boolean showCode, boolean html)
	{
		super(value);
		final int frac = currency.getDefaultFractionDigits();
		if( frac != -1 )
		{
			setMinDecimals(frac);
			setMaxDecimals(frac);
		}
		this.currency = currency;
		this.showSymbol = showSymbol;
		this.showCode = showCode;
		this.html = html;
	}

	/**
	 * @param value
	 * @param currency
	 * @param showSymbol
	 * @param showCode
	 * @param html Do not entity encode the text
	 */
	public MoneyLabel(long value, Currency currency, boolean showSymbol, boolean showCode, boolean html)
	{
		super(new BigDecimal(value).movePointLeft(currency.getDefaultFractionDigits()).doubleValue());
		final int frac = currency.getDefaultFractionDigits();
		if( frac != -1 )
		{
			setMinDecimals(frac);
			setMaxDecimals(frac);
		}
		this.currency = currency;
		this.showSymbol = showSymbol;
		this.showCode = showCode;
		this.html = html;
	}

	@SuppressWarnings("nls")
	@Override
	public String getText()
	{
		final String rawCode = currency.getCurrencyCode();
		final CurrencyInfo currencyInfo = CurrencyLabel.getCurrencyInfo(rawCode);

		if( currencyInfo == null )
		{
			return super.getText() + (showCode ? " " + rawCode.toUpperCase() : "");
		}

		final String symbol = (showSymbol ? currencyInfo.getSymbol() : "");
		final String code = (showCode ? " " + currencyInfo.getCode() : "");

		// TODO: assuming prefix symbol...
		return symbol + super.getText() + code;
	}

	@Override
	public boolean isHtml()
	{
		return html;
	}
}
