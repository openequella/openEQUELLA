package com.tle.web.cloud.search.section;

import com.tle.web.cloud.event.CloudSearchEvent;
import com.tle.web.cloud.event.CloudSearchResultsEvent;
import com.tle.web.sections.equella.listmodel.EnumListModel;
import com.tle.web.sections.equella.search.PagingSection;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

public class CloudPagingSection extends PagingSection<CloudSearchEvent, CloudSearchResultsEvent>
{

	@Override
	protected SimpleHtmlListModel<PerPageOption> getPerPageListModel()
	{
		return new EnumListModel<PerPageOption>(PER_PAGE_PFX, true, PerPageOption.MIN, PerPageOption.MIDDLE);
	}

}
