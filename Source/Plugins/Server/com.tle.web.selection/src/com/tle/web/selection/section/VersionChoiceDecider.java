package com.tle.web.selection.section;

import com.tle.beans.item.VersionSelection;
import com.tle.web.sections.SectionInfo;

public interface VersionChoiceDecider
{
	VersionSelection getVersionSelection(SectionInfo info);
}
