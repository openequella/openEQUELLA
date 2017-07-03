package com.tle.core.workflow.freetext;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;

import com.tle.common.usermanagement.user.CurrentUser;

/**
 * Lucence's StringValComparator is a final class, so simply extending it to
 * override the comparison methods not an option ... grrr
 *
 * @author larry
 */
public class CustomLuceneSortComparator extends FieldComparator<String>
{
	private String[] values;
	private String[] currentReaderValues;
	private final String field;
	private String bottom;

	public CustomLuceneSortComparator(int numHits, String field)
	{
		values = new String[numHits];
		this.field = field;
	}

	@Override
	public int compare(int slot1, int slot2)
	{
		final String val1 = values[slot1];
		final String val2 = values[slot2];
		if( val1 == null )
		{
			if( val2 == null )
			{
				return 0;
			}
			return -1;
		}
		else if( val2 == null )
		{
			return 1;
		}

		return biasedCompareTo(val1, val2);
	}

	@Override
	public int compareBottom(int doc)
	{
		final String val2 = currentReaderValues[doc];
		if( bottom == null )
		{
			if( val2 == null )
			{
				return 0;
			}
			return -1;
		}
		else if( val2 == null )
		{
			return 1;
		}
		return biasedCompareTo(bottom, val2);
	}

	@Override
	public void copy(int slot, int doc)
	{
		values[slot] = currentReaderValues[doc];
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
	public String value(int slot)
	{
		return values[slot];
	}

	@Override
	public int compareValues(String val1, String val2)
	{
		if( val1 == null )
		{
			if( val2 == null )
			{
				return 0;
			}
			return -1;
		}
		else if( val2 == null )
		{
			return 1;
		}
		else
		{
			return biasedCompareTo(val1, val2);
		}
	}

	private int biasedCompareTo(String val1, String val2)
	{
		String currentUserUuid = CurrentUser.getUserID();
		if( val1.equals(currentUserUuid) )
		{
			return -100;
		}
		if( val2.equals(currentUserUuid) )
		{
			return 100;
		}
		return val1.compareTo(val2);
	}
}
