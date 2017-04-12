package com.tle.web.institution;

import java.util.List;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

public interface Tabable extends SectionId
{
	List<Tab> getTabs(SectionInfo info);

	void gainedFocus(SectionInfo info, String tabId);

	void lostFocus(SectionInfo info, String tabId);
}
