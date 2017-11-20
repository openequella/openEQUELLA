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

package com.tle.web.lti.consumers.section;

import javax.inject.Inject;

import com.tle.common.lti.consumers.LtiConsumerConstants;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.web.entities.section.AbstractEntitySettingsLinkSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@Bind
public class LtiConsumersSettingsSection extends AbstractEntitySettingsLinkSection<Object>
{
	@PlugKey("lti.settings.title")
	private static Label TITLE_LABEL;
	@PlugKey("lti.settings.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private TLEAclManager aclManager;

	@Override
	protected HtmlLinkState getShowEntitiesLink(SectionInfo info)
	{
		return getShowConsumersLink(info);
	}

	public static HtmlLinkState getShowConsumersLink(SectionInfo info)
	{
		final HtmlLinkState state = new HtmlLinkState();
		final SectionInfo fwd = info.createForward("/access/lticonsumers.do"); //$NON-NLS-1$
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return state;
	}

	@Override
	protected Label getDescriptionLabel(SectionInfo info)
	{
		return DESCRIPTION_LABEL;
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return !aclManager.filterNonGrantedPrivileges(LtiConsumerConstants.PRIV_CREATE_CONUSMER,
			LtiConsumerConstants.PRIV_EDIT_CONSUMER).isEmpty();
	}

}
