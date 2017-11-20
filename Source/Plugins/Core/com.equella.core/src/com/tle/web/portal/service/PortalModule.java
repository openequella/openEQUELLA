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
