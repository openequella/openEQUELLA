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

package com.tle.web.externaltools.section;

import javax.inject.Inject;

import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.web.entities.section.AbstractRootEntitySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout.OneColumnLayoutModel;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@Bind
public class RootExternalToolsSection extends AbstractRootEntitySection<OneColumnLayoutModel>
{
	@PlugKey("tools.settings.title")
	private static Label TITLE_LABEL;

	@Inject
	private TLEAclManager aclManager;

	@Override
	protected boolean canView(SectionInfo info)
	{
		return !aclManager.filterNonGrantedPrivileges(ExternalToolConstants.PRIV_CREATE_TOOL,
			ExternalToolConstants.PRIV_EDIT_TOOL).isEmpty();
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return TITLE_LABEL;
	}

	@Override
	protected HtmlLinkState getShowEntitiesLink(SectionInfo info)
	{
		return ExternalToolsSettingsSection.getShowToolsLink(info);
	}
}

