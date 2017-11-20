/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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