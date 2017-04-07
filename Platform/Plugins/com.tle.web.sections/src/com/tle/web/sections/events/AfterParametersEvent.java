package com.tle.web.sections.events;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

public class AfterParametersEvent extends AbstractSectionEvent<AfterParametersListener>
{
	private ParametersEvent parametersEvent;

	public AfterParametersEvent(ParametersEvent paramEvent)
	{
		this.parametersEvent = paramEvent;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, AfterParametersListener listener) throws Exception
	{
		listener.afterParameters(info, parametersEvent);
	}

	@Override
	public Class<AfterParametersListener> getListenerClass()
	{
		return AfterParametersListener.class;
	}
}
