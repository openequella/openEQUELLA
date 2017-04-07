package com.tle.web.sections.events;

import java.util.EventListener;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

public class RespondingEvent extends AbstractSectionEvent<RespondingListener>
{

	@Override
	public void fire(SectionId sectionId, SectionInfo info, RespondingListener listener) throws Exception
	{
		listener.responding(info);
	}

	@Override
	public Class<? extends EventListener> getListenerClass()
	{
		return RespondingListener.class;
	}

}
