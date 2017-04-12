package com.tle.web.searching;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.HtmlRenderer;

public interface SearchTab extends SectionId, HtmlRenderer
{
	void setActive();

	boolean isActive();

	String getId();

	SectionInfo getForward(SectionInfo info);
}
