package com.tle.web.copyright;

import java.util.List;
import java.util.Map;

import com.tle.beans.activation.ActivateRequest;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;

@TreeIndexed
public interface CopyrightOverrideSection
{
	public void doOverride(SectionInfo info, Map<Long, List<ActivateRequest>> requestMap);
}
