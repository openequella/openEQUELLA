package com.tle.web.echo.section;

import com.tle.web.sections.SectionInfo;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

public interface ModalEchoServerSection
{
	void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs);
}
