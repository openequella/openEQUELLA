package com.tle.web.institution;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.render.Label;

public interface Tab
{
	String getId();

	Label getName();

	JSHandler getClickHandler();

	boolean shouldDefault(SectionInfo info);
}
