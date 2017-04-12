package com.tle.core.payment.beans.store;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Aaron
 */
@XmlRootElement
public class StoreCatalogueSearchBean
{
	private int start;
	private int length;
	private int available;
	private int filtered;
	private List<StoreCatalogueItemBean> results;

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

	public int getAvailable()
	{
		return available;
	}

	public void setAvailable(int available)
	{
		this.available = available;
	}

	public int getFiltered()
	{
		return filtered;
	}

	public void setFiltered(int filtered)
	{
		this.filtered = filtered;
	}

	public List<StoreCatalogueItemBean> getResults()
	{
		return results;
	}

	public void setResults(List<StoreCatalogueItemBean> results)
	{
		this.results = results;
	}
}
