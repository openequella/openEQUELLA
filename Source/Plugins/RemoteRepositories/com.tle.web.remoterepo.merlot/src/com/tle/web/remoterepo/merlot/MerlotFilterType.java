package com.tle.web.remoterepo.merlot;

import java.util.Collection;

import com.tle.common.NameValue;
import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
public interface MerlotFilterType
{
	Collection<NameValue> getValues(SectionInfo info);
}
