package com.tle.core.remoting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.tle.beans.item.ItemIdKey;

public class MatrixResults implements Serializable
{
	private static final long serialVersionUID = 1L;

	private List<String> fields;
	private List<MatrixEntry> entries = new ArrayList<MatrixEntry>();

	public static class MatrixEntry
	{
		private final List<String> fieldValues;
		private final List<ItemIdKey> items;
		private final int count;

		public MatrixEntry(final List<String> fieldValues, final List<ItemIdKey> items, int count)
		{
			this.fieldValues = fieldValues;
			this.items = items;
			this.count = count;
		}

		public List<String> getFieldValues()
		{
			return fieldValues;
		}

		public List<ItemIdKey> getItems()
		{
			return items;
		}

		public int getCount()
		{
			return count;
		}
	}

	public List<MatrixEntry> getEntries()
	{
		return entries;
	}

	public void addEntry(MatrixEntry entry)
	{
		entries.add(entry);
	}

	public void setEntries(List<MatrixEntry> entries)
	{
		this.entries = entries;
	}

	public List<String> getFields()
	{
		return fields;
	}

	public void setFields(List<String> fields)
	{
		this.fields = fields;
	}
}
