package com.tle.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author aholland
 */
public class UnmodifiableIterable<E> implements Iterable<E>
{
	protected final List<E> inner;

	public UnmodifiableIterable(Collection<E> source)
	{
		inner = new ArrayList<E>();
		for( E element : source )
		{
			inner.add(element);
		}
	}

	public int size()
	{
		return inner.size();
	}

	public E get(int index)
	{
		return inner.get(index);
	}

	@Override
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
		{
			private final Iterator<? extends E> i = inner.iterator();

			@Override
			public boolean hasNext()
			{
				return i.hasNext();
			}

			@Override
			public E next()
			{
				return i.next();
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}
}
