package com.tle.web.sections.result.util;

import java.io.Serializable;
import java.text.NumberFormat;

import com.tle.common.i18n.CurrentLocale;
import com.tle.web.sections.render.Label;

public class NumberLabel implements Label, Serializable
{
	private static final long serialVersionUID = 1L;

	protected final Number number;
	protected Integer minDecimals;
	protected Integer maxDecimals;

	public NumberLabel(Number number, int minDecimals, int maxDecimals)
	{
		this(number);
		this.minDecimals = minDecimals;
		this.maxDecimals = maxDecimals;
	}

	public NumberLabel(Number number)
	{
		this.number = number;
	}

	public Number getNumber()
	{
		return number;
	}

	public Integer getMinDecimals()
	{
		return minDecimals;
	}

	public NumberLabel setMinDecimals(Integer minDecimals)
	{
		this.minDecimals = minDecimals;
		return this;
	}

	public Integer getMaxDecimals()
	{
		return maxDecimals;
	}

	public NumberLabel setMaxDecimals(Integer maxDecimals)
	{
		this.maxDecimals = maxDecimals;
		return this;
	}

	@Override
	public String getText()
	{
		final NumberFormat numberFormat = NumberFormat.getInstance(CurrentLocale.getLocale());
		if( minDecimals != null )
		{
			numberFormat.setMinimumFractionDigits(minDecimals);
		}
		if( maxDecimals != null )
		{
			numberFormat.setMaximumFractionDigits(maxDecimals);
		}
		return numberFormat.format(number);
	}

	@Override
	public boolean isHtml()
	{
		return false;
	}
}
