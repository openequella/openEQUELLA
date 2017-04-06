package com.tle.web.search.sort;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.BroadcastEventListener;

@NonNullByDefault
public interface SortOptionsListener extends BroadcastEventListener
{
	@Nullable
	Iterable<SortOption> addSortOptions(SectionInfo info, AbstractSortOptionsSection<?> sortSection);
}