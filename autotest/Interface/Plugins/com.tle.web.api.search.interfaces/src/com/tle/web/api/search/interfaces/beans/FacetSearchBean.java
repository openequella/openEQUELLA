package com.tle.web.api.search.interfaces.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.RestBean;

/**
 * @author Dustin<br>
 * <br>
 *         A bit pointless right now, might add to it later though
 */
@XmlRootElement
public class FacetSearchBean implements RestBean
{
	private List<FacetBean> results;

	public List<FacetBean> getResults()
	{
		return results;
	}

	public FacetSearchBean setResults(List<FacetBean> results)
	{
		this.results = results;
		return this;
	}
}
