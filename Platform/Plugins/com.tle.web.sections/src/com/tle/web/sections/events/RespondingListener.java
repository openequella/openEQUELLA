package com.tle.web.sections.events;

import com.tle.web.sections.SectionInfo;

public interface RespondingListener extends BroadcastEventListener
{
	void responding(SectionInfo info);

}
