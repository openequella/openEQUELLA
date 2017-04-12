package com.tle.web.remoterepo.z3950;

import com.tle.core.fedsearch.RemoteRepoSearchResult;
import com.tle.web.remoterepo.event.RemoteRepoSearchResultEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.PagingSection;

/**
 * @author Aaron
 */
public class Z3950PagingSection
	extends
		PagingSection<Z3950SearchEvent, RemoteRepoSearchResultEvent<RemoteRepoSearchResult>>
{
	@Override
	protected int getPerPage(SectionInfo info)
	{
		PerPageOption val = getPerPage().getSelectedValue(info);

		switch( val )
		{
			case MAX:
				return 30;
			case MIDDLE:
				return 20;
			case MIN:
				return 10;
			default:
				return 10;

		}
	}
}
