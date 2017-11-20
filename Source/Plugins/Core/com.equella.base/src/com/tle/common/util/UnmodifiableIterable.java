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
