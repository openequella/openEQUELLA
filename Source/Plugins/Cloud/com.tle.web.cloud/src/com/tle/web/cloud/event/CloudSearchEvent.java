package com.tle.web.cloud.event;

import java.util.List;

import com.tle.common.searching.SortField;
import com.tle.core.cloud.search.CloudSearch;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;

public class CloudSearchEvent extends AbstractSearchEvent<CloudSearchEvent>
{
	private CloudSearch cloudSearch = new CloudSearch();

	public CloudSearchEvent(SectionId sectionId)
	{
		super(sectionId);
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchEventListener<CloudSearchEvent> listener)
		throws Exception
	{
		listener.prepareSearch(info, this);
	}

	@Override
	public void filterByTextQuery(String query, boolean includeUnfiltered)
	{
		cloudSearch.setQuery(query);
	}

	public CloudSearch getCloudSearch()
	{
		return cloudSearch;
	}

	public void filterByLanguage(String language)
	{
		cloudSearch.setLanguage(language);
	}

	public void filterByLicence(String licence)
	{
		cloudSearch.setLicence(licence);
	}

	public void filterByPublisher(String publisher)
	{
		cloudSearch.setPublisher(publisher);
	}

	public void filterByEducationLevel(String educationLevel)
	{
		cloudSearch.setEducationLevel(educationLevel);
	}

	public void filterByFormats(List<String> formats)
	{
		cloudSearch.addFormats(formats);
	}

	@Override
	public void setSortFields(SortField... sort)
	{
		cloudSearch.setSortFields(sort);
	}

}
