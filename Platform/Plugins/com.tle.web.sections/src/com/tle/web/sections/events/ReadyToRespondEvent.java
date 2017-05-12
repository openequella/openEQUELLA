package com.tle.web.sections.events;

import java.util.EventListener;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

public class ReadyToRespondEvent extends AbstractSectionEvent<ReadyToRespondListener>
{
	private boolean redirect;

	public ReadyToRespondEvent(boolean redirect)
	{
		this.redirect = redirect;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, ReadyToRespondListener listener) throws Exception
	{
		listener.readyToRespond(info, redirect);
	}

	@Override
	public Class<? extends EventListener> getListenerClass()
	{
		return ReadyToRespondListener.class;
	}

}
