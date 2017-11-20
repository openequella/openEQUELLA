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
