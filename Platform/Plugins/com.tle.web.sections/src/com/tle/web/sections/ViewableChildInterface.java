package com.tle.web.sections;

import com.tle.annotation.NonNullByDefault;

@NonNullByDefault
public interface ViewableChildInterface
{
	boolean canView(SectionInfo info);
}
