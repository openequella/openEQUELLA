package com.tle.web.oauth.section;

import com.tle.web.sections.SectionInfo;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author Aaron
 */
public interface ModalOAuthSection
{
	void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs);
}
