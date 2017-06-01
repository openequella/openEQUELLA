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

package com.tle.web.viewitem.section;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderedPathMapper<T> extends PathMapper<T>
{
	private Set<T> alwaysSet;
	private Comparator<? super T> comparator;

	public OrderedPathMapper(Comparator<? super T> comparator)
	{
		alwaysSet = new HashSet<T>();
		this.comparator = comparator;
	}

	@Override
	public void addMapping(Type type, String path, T object)
	{
		if( type == Type.ALWAYS )
		{
			alwaysSet.add(object);
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}

	public Collection<T> getMatchingFilters(String path, String mimeType)
	{
		List<T> results = new ArrayList<T>();
		results.addAll(alwaysSet);
		Collections.sort(results, comparator);
		return results;
	}
}
