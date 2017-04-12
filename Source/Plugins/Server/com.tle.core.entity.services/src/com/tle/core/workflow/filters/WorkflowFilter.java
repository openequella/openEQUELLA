package com.tle.core.workflow.filters;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.tle.beans.item.ItemKey;
import com.tle.core.workflow.operations.WorkflowOperation;

public interface WorkflowFilter
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
