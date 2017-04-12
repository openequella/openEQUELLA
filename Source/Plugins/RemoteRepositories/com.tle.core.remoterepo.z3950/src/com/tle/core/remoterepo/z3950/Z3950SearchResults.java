package com.tle.core.remoterepo.z3950;

import java.util.List;

import com.tle.common.searching.SimpleSearchResults;

/**
 * @author aholland
 */
public class Z3950SearchResults extends SimpleSearchResults<Z3950SearchResult>
{
	private static final long serialVersionUID = 1L;

	public Z3950SearchResults(List<Z3950SearchResult> results, int count, int offset, int available)
	{
		super(results, count, offset, available);
	}
}
