package com.tle.web.sections.js;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public interface ElementId
{
	boolean isStaticId();

	String getElementId(SectionInfo info);

	void registerUse();

	boolean isElementUsed();
}
