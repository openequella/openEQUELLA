package com.tle.common.searching;

import java.util.List;

/**
 * @author aholland
 */
public class SimpleSearchResults<T> implements SearchResults<T>
{
	private static final long serialVersionUID = 1L;

	private final int count;
	private final int offset;
	private final int available;
	private final List<T> results;
	private String errorMessage;

	public SimpleSearchResults(List<T> results, int count, int offset, int available)
	{
		this.count = count;
		this.offset = offset;
		this.available = available;
		this.results = results;
	}

	@Override
	public int getAvailable()
	{
		return available;
	}

	@Override
	public int getCount()
	{
		return count;
	}

	@Override
	public int getOffset()
	{
		return offset;
	}

	@Override
	public List<T> getResults()
	{
		return results;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}
}
