package com.tle.common.search;

import java.util.List;

import com.tle.common.searching.SimpleSearchResults;

public abstract class AbstractItemSearchResults<T> extends SimpleSearchResults<T>
{
	private static final long serialVersionUID = 1L;

	public AbstractItemSearchResults(List<T> results, int count, int offset, int available)
	{
		super(results, count, offset, available);
	}
}
