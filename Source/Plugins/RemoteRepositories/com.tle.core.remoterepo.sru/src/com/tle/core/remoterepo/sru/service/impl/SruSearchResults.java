/**
 * 
 */
package com.tle.core.remoterepo.sru.service.impl;

import java.util.List;

import com.tle.common.searching.SimpleSearchResults;

/**
 * @author larry
 */
public class SruSearchResults extends SimpleSearchResults<SruSearchResult>
{
	private static final long serialVersionUID = 1L;

	public SruSearchResults(List<SruSearchResult> results, int count, int offset, int available)
	{
		super(results, count, offset, available);
	}
}
