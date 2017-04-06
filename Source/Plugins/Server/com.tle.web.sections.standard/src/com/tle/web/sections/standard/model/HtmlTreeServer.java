package com.tle.web.sections.standard.model;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.js.JSCallable;

@NonNullByDefault
public interface HtmlTreeServer
{
	Bookmark getAjaxUrlForNode(SectionInfo info, String nodeId);

	JSCallable getAjaxFunctionForNode(SectionInfo info, String nodeId);

}
