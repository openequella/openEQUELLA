package com.tle.common.filters;

public class OrFilter<T> implements Filter<T>
{
	private final Filter<T>[] filters;

	public static <S> OrFilter<S> create(Filter<S>... filters)
	{
		return new OrFilter<S>(filters);
	}

	public OrFilter(Filter<T>... filters)
	{
		this.filters = filters;
	}

	@Override
	public boolean include(T t)
	{
		for( Filter<T> filter : filters )
		{
			if( filter != null && filter.include(t) )
			{
				return true;
			}
		}
		return false;
	}
}