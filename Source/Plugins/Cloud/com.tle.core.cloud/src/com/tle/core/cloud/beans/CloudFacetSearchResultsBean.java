package com.tle.core.cloud.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CloudFacetSearchResultsBean
{
	private List<CloudFacetBean> results;

	public List<CloudFacetBean> getResults()
	{
		return results;
	}

	public void setResults(List<CloudFacetBean> results)
	{
		this.results = results;
	}
}
