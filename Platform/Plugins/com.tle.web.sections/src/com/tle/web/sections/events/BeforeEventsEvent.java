package com.tle.web.sections.events;

import java.util.EventListener;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

public class BeforeEventsEvent extends AbstractSectionEvent<BeforeEventsListener>
{

	@Override
	public void fire(SectionId sectionId, SectionInfo info, BeforeEventsListener listener) throws Exception
	{
		listener.beforeEvents(info);
	}

	@Override
	public Class<? extends EventListener> getListenerClass()
	{
		return BeforeEventsListener.class;
	}

	@Override
	public boolean isContinueAfterException()
	{
		return true;
	}

}
