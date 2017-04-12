package com.tle.web.navigation;

import java.util.Map;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.TagState;

/**
 * @author aholland
 */
public interface BreadcrumbProvider
{
	TagState getBreadcrumb(SectionInfo info, Map<String, ?> params);
}
