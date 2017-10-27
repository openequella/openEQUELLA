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

package com.tle.core.workflow.freetext;

import java.io.IOException;

import com.tle.common.Check;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;

public final class CustomLuceneSortComparator extends FieldComparator<Integer>
{

	private final String userId;
	private int[] values;
	private String[] currentReaderValues;
	private final String field;
	private int bottom;

	public CustomLuceneSortComparator(int numHits, String field, String userId)
	{
		values = new int[numHits];
		this.field = field;
		this.userId = userId;
	}

	@Override
	public int compare(int slot1, int slot2)
	{
		final int val1 = values[slot1];
		final int val2 = values[slot2];
		return val1 - val2;
	}

	public int isCurrentUser(String userId)
	{
		return this.userId.equals(userId) ? 0 : Check.isEmpty(userId) ? 1 : 2;
	}

	@Override
	public int compareBottom(int doc)
	{
		final int val2 = isCurrentUser(currentReaderValues[doc]);
		return bottom - val2;
	}

	@Override
	public void copy(int slot, int doc)
	{
		values[slot] = isCurrentUser(currentReaderValues[doc]);
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException
	{
		currentReaderValues = FieldCache.DEFAULT.getStrings(reader, field);
	}

	@Override
	public void setBottom(final int bottom)
	{
		this.bottom = values[bottom];
	}

	@Override
	public Integer value(int slot)
	{
		return values[slot];
	}

	@Override
	public int compareValues(Integer val1, Integer val2)
	{
		return val1 - val2;
	}
}
