package com.tle.core.cloud.service;

import java.util.List;

import com.tle.common.searching.SimpleSearchResults;
import com.tle.core.cloud.beans.converted.CloudItem;

/**
 * @author Aaron
 */
public class CloudSearchResults extends SimpleSearchResults<CloudItem>
{
	private final int filteredOut;

	public CloudSearchResults(List<CloudItem> results, int count, int offset, int available, int filteredOut)
	{
		super(results, count, offset, available);
		this.filteredOut = filteredOut;
	}

	public int getFilteredOut()
	{
		return filteredOut;
	}
}
