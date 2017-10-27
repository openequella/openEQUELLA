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

package com.tle.core.item.operations;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.tle.beans.item.ItemKey;

public interface ItemOperationFilter
{
	WorkflowOperation[] getOperations();

	void setDateNow(Date now);

	boolean isReadOnly();

	FilterResults getItemIds();

	public class FilterResults
	{
		private final long total;
		private final Iterator<? extends ItemKey> results;

		public FilterResults(long total, Iterator<? extends ItemKey> results)
		{
			this.total = total;
			this.results = results;
		}

		public FilterResults(Collection<? extends ItemKey> results)
		{
			this(results.size(), results.iterator());
		}

		public long getTotal()
		{
			return total;
		}

		public Iterator<? extends ItemKey> getResults()
		{
			return results;
		}
	}
}
