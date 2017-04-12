package com.tle.web.myresources;

import com.tle.common.searching.Search.SortType;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.sort.StandardSortOptionsSection;
import com.tle.web.sections.SectionInfo;

public class MyResourcesSortSection extends StandardSortOptionsSection<FreetextSearchEvent>
{
	private static final String DATE_VALUE = SortType.DATEMODIFIED.name().toLowerCase();

	@Override
	protected String getDefaultSearch(SectionInfo info)
	{
		return DATE_VALUE;
	}
}
