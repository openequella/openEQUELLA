package com.tle.web.sections.events.js;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.SectionEvent;

@NonNullByDefault
public interface ParameterizedEvent
{
	int getParameterCount();

	String getEventId();

	SectionEvent<?> createEvent(SectionInfo info, String[] params);

	boolean isPreventXsrf();
}
