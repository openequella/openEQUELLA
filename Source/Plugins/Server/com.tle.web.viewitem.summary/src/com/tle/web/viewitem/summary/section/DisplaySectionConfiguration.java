package com.tle.web.viewitem.summary.section;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.web.sections.SectionId;

@NonNullByDefault
public interface DisplaySectionConfiguration extends SectionId
{
	void associateConfiguration(SummarySectionsConfig config);
}
