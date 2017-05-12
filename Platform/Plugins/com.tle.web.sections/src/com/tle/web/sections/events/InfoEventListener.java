package com.tle.web.sections.events;

import com.tle.web.sections.MutableSectionInfo;

public interface InfoEventListener extends BroadcastEventListener
{
	void handleInfoEvent(MutableSectionInfo info, boolean removed, boolean processParameters);
}
