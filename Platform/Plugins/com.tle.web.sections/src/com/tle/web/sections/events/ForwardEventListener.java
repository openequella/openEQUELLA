package com.tle.web.sections.events;

import com.tle.web.sections.SectionInfo;

public interface ForwardEventListener extends BroadcastEventListener
{
	void forwardCreated(SectionInfo info, SectionInfo forward);
}
