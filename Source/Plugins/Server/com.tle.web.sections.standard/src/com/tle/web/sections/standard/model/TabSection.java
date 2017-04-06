package com.tle.web.sections.standard.model;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.NameValue;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public interface TabSection extends SectionId
{
	NameValue getTabToAppearOn();

	boolean isVisible(SectionInfo info);
}
