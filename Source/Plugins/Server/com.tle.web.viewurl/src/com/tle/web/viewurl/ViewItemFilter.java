package com.tle.web.viewurl;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.generic.NumberOrder;

public interface ViewItemFilter extends NumberOrder
{
	ViewItemResource filter(SectionInfo info, ViewItemResource resource);
}
