package com.tle.web.sections.standard;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public interface RendererSelectable
{
	void setRendererType(SectionInfo info, String type);
}
