package com.tle.web.sections.events;

import com.tle.web.sections.SectionInfo;

public interface AfterParametersListener extends BroadcastEventListener
{
	void afterParameters(SectionInfo info, ParametersEvent event);
}
