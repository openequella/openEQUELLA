package com.tle.web.search.filter;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.BroadcastEventListener;

@NonNullByDefault
public interface ResetFiltersListener extends BroadcastEventListener
{
	void reset(SectionInfo info);
}
