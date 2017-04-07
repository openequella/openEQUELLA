package com.tle.web.sections.events;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

public class ForwardEvent extends AbstractSectionEvent<ForwardEventListener>
{
	private final SectionInfo forward;

	public ForwardEvent(SectionInfo forward)
	{
		this.forward = forward;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, ForwardEventListener listener) throws Exception
	{
		listener.forwardCreated(info, forward);
	}

	@Override
	public Class<ForwardEventListener> getListenerClass()
	{
		return ForwardEventListener.class;
	}
}
