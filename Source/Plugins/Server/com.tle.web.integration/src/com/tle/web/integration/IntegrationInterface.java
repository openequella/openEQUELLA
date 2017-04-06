package com.tle.web.integration;

import com.tle.common.NameValue;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.layout.LayoutSelector;
import com.tle.web.selection.SelectionSession;

public interface IntegrationInterface
{
	String getClose();

	String getCourseInfoCode();

	NameValue getLocation();

	LayoutSelector createLayoutSelector(SectionInfo info);

	void select(SectionInfo info, SelectionSession session);
}
