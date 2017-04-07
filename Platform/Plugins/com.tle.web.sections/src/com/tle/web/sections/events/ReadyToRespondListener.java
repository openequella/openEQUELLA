package com.tle.web.sections.events;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public interface ReadyToRespondListener extends BroadcastEventListener
{
	void readyToRespond(SectionInfo info, boolean redirect);
}
