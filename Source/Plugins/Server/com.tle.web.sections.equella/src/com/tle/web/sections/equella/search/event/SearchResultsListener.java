package com.tle.web.sections.equella.search.event;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.BroadcastEventListener;

@NonNullByDefault
public interface SearchResultsListener<E extends AbstractSearchResultsEvent<E>> extends BroadcastEventListener
{
	void processResults(SectionInfo info, E results);

}
