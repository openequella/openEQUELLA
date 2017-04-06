package com.tle.web.selection;

import com.tle.web.sections.SectionInfo;

public interface SelectableInterface
{
	SectionInfo createSectionInfo(SectionInfo info, SelectionSession session);
}
