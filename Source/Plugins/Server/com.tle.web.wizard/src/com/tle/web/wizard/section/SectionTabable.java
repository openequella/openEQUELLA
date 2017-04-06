package com.tle.web.wizard.section;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public interface SectionTabable extends Section
{
	void addTabs(SectionInfo info, List<SectionTab> tabs);

	void setupShowingTab(SectionInfo info, SectionTab tab);

	void leavingTab(SectionInfo info, SectionTab tab);

	void unfinishedTab(SectionInfo info, SectionTab tab);
}
