package com.tle.core.payment;

import java.util.List;

import com.tle.common.payment.entity.Sale;

/**
 * @author Aaron
 */
public class SaleSearchResults
{
	private int offset;
	private int count;
	private int available;
	private List<Sale> results;

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public int getAvailable()
	{
		return available;
	}

	public void setAvailable(int available)
	{
		this.available = available;
	}

	public List<Sale> getResults()
	{
		return results;
	}

	public void setResults(List<Sale> results)
	{
		this.results = results;
	}
}
