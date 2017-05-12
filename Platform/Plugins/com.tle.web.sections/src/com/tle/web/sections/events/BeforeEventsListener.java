package com.tle.web.sections.events;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public interface BeforeEventsListener extends BroadcastEventListener
{
	void beforeEvents(SectionInfo info);
}
