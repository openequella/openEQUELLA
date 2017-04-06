package com.tle.web.payment.storefront.section.workflow;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface ModalApprovalSection
{
	void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs);
}
