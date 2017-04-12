package com.tle.web.controls.flickr;

import com.tle.web.sections.equella.search.PagingSection;
import com.tle.web.sections.standard.Pager;
import com.tle.web.sections.standard.annotations.Component;

public class FlickrPagingSection extends PagingSection<FlickrSearchEvent, FlickrSearchResultsEvent>
{
	@Component(name = "p")
	private Pager flickrPager;

	@Override
	public Pager getPager()
	{
		return flickrPager;
	}
}
