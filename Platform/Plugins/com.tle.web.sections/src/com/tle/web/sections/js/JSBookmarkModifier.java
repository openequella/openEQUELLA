package com.tle.web.sections.js;

import java.util.Map;

import com.tle.web.sections.BookmarkModifier;

public interface JSBookmarkModifier extends BookmarkModifier
{
	boolean hasClientModifications();

	Map<String, JSExpression> getClientExpressions();

	String getEventId();

	JSExpression[] getParameters();
}
