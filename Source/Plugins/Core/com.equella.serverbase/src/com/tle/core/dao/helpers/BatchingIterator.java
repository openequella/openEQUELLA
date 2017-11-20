/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
