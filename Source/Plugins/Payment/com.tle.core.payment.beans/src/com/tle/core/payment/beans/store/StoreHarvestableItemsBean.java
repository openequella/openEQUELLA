/**
 *
 */
package com.tle.core.payment.beans.store;

import java.util.List;

/**
 * @author larry
 */
public class StoreHarvestableItemsBean
{
	private int start;
	private int length;
	private long available;
	private List<StoreHarvestableItemBean> results;

	public int getStart()
	{
		return start;
	}

	public void setStart(int start)
	{
		this.start = start;
	}

	public int getLength()
	{
		return length;
	}

	public void setLength(int length)
	{
		this.length = length;
	}

	public long getAvailable()
	{
		return available;
	}

	public void setAvailable(long available)
	{
		this.available = available;
	}

	public List<StoreHarvestableItemBean> getResults()
	{
		return results;
	}

	public void setResults(List<StoreHarvestableItemBean> results)
	{
		this.results = results;
	}
}
