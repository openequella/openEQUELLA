package com.tle.web.search.sort;

import javax.inject.Inject;

import com.tle.beans.system.SearchSettings;
import com.tle.core.services.config.ConfigurationService;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;

public class SortOptionsSection extends StandardSortOptionsSection<FreetextSearchEvent>
{
	@Inject
	private ConfigurationService configService;

	@Override
	protected String getDefaultSearch(SectionInfo info)
	{
		if( !info.getBooleanAttribute(SectionInfo.KEY_FOR_URLS_ONLY) )
		{
			String defaultSort = configService.getProperties(new SearchSettings()).getDefaultSearchSort();
			if( defaultSort != null )
			{
				return defaultSort.toLowerCase();
			}
		}
		return null;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		// So that it gets cached in the context after the first time
		sortOptions.setAlwaysSelectForBookmark(true);
	}
}
