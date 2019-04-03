package com.tle.core.connectors.blackboard.beans;

import java.util.List;

public abstract class PagedResults<M>
{
	private List<M> results;
	private Paging paging;

	public List<M> getResults()
	{
		return results;
	}

	public void setResults(List<M> results)
	{
		this.results = results;
	}

	public Paging getPaging()
	{
		return paging;
	}

	public void setPaging(Paging paging)
	{
		this.paging = paging;
	}
}
