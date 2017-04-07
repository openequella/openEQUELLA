package com.tle.web.sections;

import java.util.Map;

public interface BookmarkModifier
{
	void addToBookmark(SectionInfo info, Map<String, String[]> bookmarkState);

}
