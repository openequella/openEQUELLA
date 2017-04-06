package com.tle.web.viewitem;

import com.tle.web.sections.SectionInfo;
import com.tle.web.stream.ContentStream;

public interface ContentFilter
{
	ContentStream filterContent(SectionInfo info, ContentStream stream);
}
