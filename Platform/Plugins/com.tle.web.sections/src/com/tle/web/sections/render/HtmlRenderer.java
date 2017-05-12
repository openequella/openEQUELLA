package com.tle.web.sections.render;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;

@NonNullByDefault
public interface HtmlRenderer
{
	@Nullable
	SectionResult renderHtml(RenderEventContext context) throws Exception;
}
