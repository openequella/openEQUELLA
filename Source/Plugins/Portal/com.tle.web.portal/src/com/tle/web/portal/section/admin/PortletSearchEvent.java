package com.tle.web.portal.section.admin;

import com.tle.core.portal.service.PortletSearch;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;

/**
 * @author aholland
 */
public class PortletSearchEvent extends AbstractSearchEvent<PortletSearchEvent>
{
	private final PortletSearch search;
	private final PortletSearch unfiltered;

	public PortletSearchEvent(SectionId sectionId, PortletSearch search, PortletSearch unfiltered)
	{
		super(sectionId);
		this.search = search;
		this.unfiltered = unfiltered;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchEventListener<PortletSearchEvent> listener)
		throws Exception
	{
		listener.prepareSearch(info, this);
	}

	public PortletSearch getSearch()
	{
		return search;
	}

	public PortletSearch getUnfilteredSearch()
	{
		return unfiltered;
	}

	public void filterByOwner(String uuid)
	{
		search.setOwner(uuid);
	}

	public void filterByType(String type)
	{
		search.setType(type);
	}

	public void filterByOnlyInstWide(Boolean checked)
	{
		search.setOnlyInstWide(checked);
	}
}
