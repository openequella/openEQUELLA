package com.tle.web.searching.section;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;
import com.tle.web.searching.SearchTab;
import com.tle.web.searching.StandardResultsTab;
import com.tle.web.searching.section.AbstractSearchTabsSection.SearchTabsModel;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
public class SearchTabsSection extends AbstractSearchTabsSection<SearchTabsModel>
{
	@Override
	protected Class<? extends SearchTab> getActiveSearchTabClass()
	{
		return StandardResultsTab.class;
	}
}