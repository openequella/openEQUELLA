package com.tle.web.sections.standard.model;

import java.util.List;

import com.tle.web.sections.SectionInfo;

public interface TabModel
{
	List<TabContent> getVisibleTabs(SectionInfo info);

	int getIndexForTab(SectionInfo info, String tabId);

}
