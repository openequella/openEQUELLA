package com.tle.web.sections.events;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public interface ParametersEventListener extends BroadcastEventListener
{
	void handleParameters(SectionInfo info, ParametersEvent event) throws Exception;
}
