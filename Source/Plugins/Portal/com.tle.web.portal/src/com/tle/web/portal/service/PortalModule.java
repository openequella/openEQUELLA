package com.tle.web.portal.service;

import com.google.inject.name.Names;
import com.tle.web.portal.section.common.PortletContributionSection;
import com.tle.web.portal.section.common.SelectNewPortletTypeSection;
import com.tle.web.portal.section.enduser.RestoreInstitutionalPortletSection;
import com.tle.web.portal.section.enduser.RootPortletSection;
import com.tle.web.portal.section.enduser.ShowPortletsSection;
import com.tle.web.sections.equella.guice.SectionsModule;

@SuppressWarnings("nls")
public class PortalModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("portalTree")).toProvider(portalTree());
	}

	private NodeProvider portalTree()
	{
		NodeProvider node = node(RootPortletSection.class);
		node.innerChild(PortletContributionSection.class);
		node.child(SelectNewPortletTypeSection.class);
		node.child(RestoreInstitutionalPortletSection.class);
		node.child(ShowPortletsSection.class);
		return node;
	}
}
