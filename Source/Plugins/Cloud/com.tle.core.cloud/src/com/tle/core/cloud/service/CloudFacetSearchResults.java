package com.tle.core.cloud.service;

import java.util.List;

import com.tle.common.searching.SimpleSearchResults;
import com.tle.core.cloud.beans.CloudFacetBean;

public class CloudFacetSearchResults extends SimpleSearchResults<CloudFacetBean>
{

	public CloudFacetSearchResults(List<CloudFacetBean> results)
	{
		super(results, 0, 0, results.size());
	}

}
