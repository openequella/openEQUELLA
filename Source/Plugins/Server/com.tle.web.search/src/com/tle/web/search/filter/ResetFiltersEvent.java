package com.tle.web.search.filter;

import java.util.EventListener;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.AbstractSectionEvent;

public class ResetFiltersEvent extends AbstractSectionEvent<ResetFiltersListener>
{

	@Override
	public Class<? extends EventListener> getListenerClass()
	{
		return ResetFiltersListener.class;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, ResetFiltersListener listener) throws Exception
	{
		listener.reset(info);
	}

}
