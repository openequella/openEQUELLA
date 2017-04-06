package com.tle.web.portal.section.enduser;

import com.tle.web.sections.SectionInfo;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

public interface ModalPortletSection
{
	void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs);
}
