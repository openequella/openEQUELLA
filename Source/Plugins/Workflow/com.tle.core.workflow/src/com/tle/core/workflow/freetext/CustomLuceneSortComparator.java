package com.tle.core.workflow.freetext;

import java.io.IOException;

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
		return this.userId.equals(userId) ? 0 : 1;
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
