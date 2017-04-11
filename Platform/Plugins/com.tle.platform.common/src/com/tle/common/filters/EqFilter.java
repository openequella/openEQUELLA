package com.tle.common.filters;

import com.tle.common.Check;

public class EqFilter<T> implements Filter<T>
{
	private final Object v;

	public EqFilter(Object v)
	{
		this.v = v;
	}

	@Override
	public boolean include(T t)
	{
		return Check.bothNullOrEqual(getForComparison(t), v);
	}

	protected Object getForComparison(T t)
	{
		return t;
	}
}