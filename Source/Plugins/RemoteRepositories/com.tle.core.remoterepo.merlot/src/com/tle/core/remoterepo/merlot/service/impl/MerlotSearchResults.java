package com.tle.core.remoterepo.merlot.service.impl;

import java.util.List;

import com.tle.common.searching.SimpleSearchResults;

/**
 * @author aholland
 */
public class MerlotSearchResults extends SimpleSearchResults<MerlotSearchResult>
{
	private static final long serialVersionUID = 1L;

	protected MerlotSearchResults(List<MerlotSearchResult> results, int count, int offset, int available)
	{
		super(results, count, offset, available);
	}
}
