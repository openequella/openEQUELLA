package com.tle.web.portal.section.admin.filter;

import com.tle.web.portal.section.admin.PortletSearchEvent;
import com.tle.web.search.filter.AbstractFilterByUserSection;
import com.tle.web.sections.SectionInfo;

public class FilterPortletByOwnerSection extends AbstractFilterByUserSection<PortletSearchEvent>
{
	@Override
	public void prepareSearch(SectionInfo info, PortletSearchEvent event) throws Exception
	{
		event.filterByOwner(getSelectedUserId(info));
	}

	@Override
	protected String getPublicParam()
	{
		return "owner"; //$NON-NLS-1$
	}

	@Override
	public String getAjaxDiv()
	{
		return "owner"; //$NON-NLS-1$
	}

}