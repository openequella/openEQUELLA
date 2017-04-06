package com.tle.core.dao.helpers;

import java.util.Iterator;

import com.google.common.base.Optional;

/**
 * Implements iterator and iterable over a possibly large set of results that
 * can be retrieved/generated in batches.
 */
public abstract class BatchingIterator<T> implements Iterator<T>, Iterable<T>
{
	private Iterator<T> batch;
	private boolean finished = false;
	private Optional<T> lastObj = Optional.absent();

	@Override
	public Iterator<T> iterator()
	{
		return this;
	}

	@Override
	public boolean hasNext()
	{
		if( finished )
		{
			return false;
		}

		if( batch != null && batch.hasNext() )
		{
			return batch.hasNext();
		}

		// Update state with a fresh batch and recurse
		batch = getMore(lastObj);
		finished = batch == null || !batch.hasNext();
		return hasNext();
	}

	@Override
	public T next()
	{
		T value = batch.next();
		if( !batch.hasNext() )
		{
			lastObj = Optional.of(value);
		}
		return value;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @param obj this will be null for the first round.
	 */
	protected abstract Iterator<T> getMore(Optional<T> lastObjGivenToUser);
}
