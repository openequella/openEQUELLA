package com.tle.common.filters;

public class AndFilter<T> implements Filter<T>
{
	private final Filter<T>[] filters;

	public static <S> AndFilter<S> create(Filter<S>... filters)
	{
		return new AndFilter<S>(filters);
	}

	public AndFilter(Filter<T>... filters)
	{
		this.filters = filters;
	}

	@Override
	public boolean include(T t)
	{
		for( Filter<T> filter : filters )
		{
			if( filter != null && !filter.include(t) )
			{
				return false;
			}
		}
		return true;
	}
}