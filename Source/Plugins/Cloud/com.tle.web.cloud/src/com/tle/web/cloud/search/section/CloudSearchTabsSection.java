package com.tle.web.cloud.search.section;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;
import com.tle.web.cloud.search.CloudResultsTab;
import com.tle.web.searching.SearchTab;
import com.tle.web.searching.section.AbstractSearchTabsSection;
import com.tle.web.searching.section.AbstractSearchTabsSection.SearchTabsModel;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
public class CloudSearchTabsSection extends AbstractSearchTabsSection<SearchTabsModel>
{
	@Override
	protected Class<? extends SearchTab> getActiveSearchTabClass()
	{
		return CloudResultsTab.class;
	}
}
