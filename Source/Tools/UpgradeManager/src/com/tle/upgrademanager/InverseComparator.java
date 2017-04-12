package com.tle.upgrademanager;

import java.util.Comparator;

public class InverseComparator<T> implements Comparator<T>
{
	private final Comparator<T> delegate;

	public InverseComparator(Comparator<T> delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public int compare(T o1, T o2)
	{
		return -delegate.compare(o1, o2);
	}
}
