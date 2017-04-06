package com.tle.web.sections.equella.search.event;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.TargetedEventListener;

@NonNullByDefault
public interface SearchEventListener<E extends AbstractSearchEvent<E>> extends TargetedEventListener
{
	void prepareSearch(SectionInfo info, E event) throws Exception;
}
