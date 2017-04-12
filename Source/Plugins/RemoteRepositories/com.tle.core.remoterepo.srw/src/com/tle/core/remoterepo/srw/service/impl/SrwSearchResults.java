package com.tle.core.remoterepo.srw.service.impl;

import java.util.List;

import com.tle.common.searching.SimpleSearchResults;

/**
 * @author aholland
 */
public class SrwSearchResults extends SimpleSearchResults<SrwSearchResult>
{
	private static final long serialVersionUID = 1L;

	protected SrwSearchResults(List<SrwSearchResult> results, int count, int offset, int available)
	{
		super(results, count, offset, available);
	}
}
