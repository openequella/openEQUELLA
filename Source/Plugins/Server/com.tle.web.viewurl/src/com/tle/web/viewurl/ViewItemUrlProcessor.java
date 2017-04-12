package com.tle.web.viewurl;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;

@TreeIndexed
public interface ViewItemUrlProcessor extends SectionId
{
	void processModel(SectionInfo info, ViewItemUrl viewItemUrl);
}
