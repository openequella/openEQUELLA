package com.tle.web.sections.events;

import com.tle.web.sections.BookmarkModifier;
import com.tle.web.sections.SectionInfo;

public interface EventAuthoriser extends BookmarkModifier
{
	void checkAuthorisation(SectionInfo info);
}
