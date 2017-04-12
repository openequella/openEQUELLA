package com.tle.web.integration;

import com.tle.common.NameValue;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.layout.LayoutSelector;
import com.tle.web.selection.SelectionSession;

public interface IntegrationInterface
{
	IntegrationSessionData getData();

	String getClose();

	String getCourseInfoCode();

	NameValue getLocation();

	LayoutSelector createLayoutSelector(SectionInfo info);

	/**
	 * 
	 * @param info
	 * @param session
	 * @return true if you want to maintain selected resources, otherwise false
	 */
	boolean select(SectionInfo info, SelectionSession session);
}
