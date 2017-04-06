package com.tle.core.payment.beans.store;

import java.math.BigDecimal;

/**
 * @author Aaron
 */
public class DecimalNumberBean
{
	private long value;
	private long scale;

	public DecimalNumberBean()
	{
	}

	public DecimalNumberBean(BigDecimal bd)
	{
		this(bd.unscaledValue().longValue(), bd.scale());
	}

	public DecimalNumberBean(long value, long scale)
	{
		this.value = value;
		this.scale = scale;
	}

	public long getValue()
	{
		return value;
	}

	public void setValue(long value)
	{
		this.value = value;
	}

	public long getScale()
	{
		return scale;
	}

	public void setScale(long scale)
	{
		this.scale = scale;
	}
}
