package com.tle.web.portal.service;

import com.tle.web.portal.section.admin.AdminSelectNewPortletTypeSection;
import com.tle.web.portal.section.admin.PortletResultsSection;
import com.tle.web.portal.section.admin.RootPortletAdminSection;
import com.tle.web.portal.section.admin.filter.FilterPortletByInstWideSection;
import com.tle.web.portal.section.admin.filter.FilterPortletByOwnerSection;
import com.tle.web.portal.section.admin.filter.FilterPortletByTypeSection;
import com.tle.web.portal.section.common.PortletContributionSection;
import com.tle.web.search.filter.SimpleResetFiltersQuerySection;
import com.tle.web.search.guice.AbstractSearchModule;

@SuppressWarnings("nls")
public class PortalSearchModule extends AbstractSearchModule
{
	@Override
	protected NodeProvider getRootNode()
	{
		NodeProvider node = node(RootPortletAdminSection.class);
		node.innerChild(PortletContributionSection.class);
		node.child(AdminSelectNewPortletTypeSection.class);
		return node;
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(SimpleResetFiltersQuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(PortletResultsSection.class);
	}

	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(FilterPortletByOwnerSection.class);
		node.child(FilterPortletByTypeSection.class);
		node.child(FilterPortletByInstWideSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return "portalAdminTree";
	}
}