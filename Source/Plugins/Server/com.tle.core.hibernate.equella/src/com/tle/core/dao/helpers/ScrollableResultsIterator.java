package com.tle.core.dao.helpers;

import java.util.Iterator;

import org.hibernate.ScrollableResults;

/**
 * Use for forward only scrollable results.
 * 
 * @author Aaron
 */
public class ScrollableResultsIterator<T> implements Iterator<T>
{
	private final ScrollableResults results;
	private int index = -1;

	public ScrollableResultsIterator(ScrollableResults results)
	{
		this.results = results;
	}

	@Override
	public boolean hasNext()
	{
		return results.next();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next()
	{
		index++;
		return (T) results.get(0);
	}

	@Override
	public void remove()
	{
		// No
	}

	public int getIndex()
	{
		return index;
	}
}
