package com.tle.web.mimetypes.search.event;

import com.tle.common.search.DefaultSearch;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;

public class MimeSearchEvent extends AbstractSearchEvent<MimeSearchEvent>
{
	private final DefaultSearch search;

	public MimeSearchEvent()
	{
		// broadcast
		super(null);
		search = new DefaultSearch();
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchEventListener<MimeSearchEvent> listener)
		throws Exception
	{
		listener.prepareSearch(info, this);
	}

	public DefaultSearch getSearch()
	{
		return search;
	}
}
