package com.tle.web.sections.events;

import java.util.EventListener;

import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

public class InfoEvent extends AbstractSectionEvent<InfoEventListener>
{
	private final boolean removed;
	private final boolean processParameters;

	public InfoEvent(boolean removed, boolean processParameters)
	{
		this.removed = removed;
		this.processParameters = processParameters;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, InfoEventListener listener) throws Exception
	{
		listener.handleInfoEvent(info.getAttributeForClass(MutableSectionInfo.class), removed, processParameters);
	}

	@Override
	public Class<? extends EventListener> getListenerClass()
	{
		return InfoEventListener.class;
	}

	public boolean isProcessParameters()
	{
		return processParameters;
	}
}
