package com.tle.web.sections;

import java.util.Collections;
import java.util.Map;

public class SimpleBookmarkModifier implements BookmarkModifier
{
	private final Map<String, String[]> vals;

	public SimpleBookmarkModifier(String key, String value)
	{
		vals = Collections.singletonMap(key, new String[]{value});
	}

	@Override
	public void addToBookmark(SectionInfo info, Map<String, String[]> bookmark)
	{
		bookmark.putAll(vals);
	}
}
