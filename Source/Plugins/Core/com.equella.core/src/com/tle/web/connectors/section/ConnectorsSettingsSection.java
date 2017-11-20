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

package com.tle.web.connectors.section;

import static com.tle.common.connectors.ConnectorConstants.PRIV_CREATE_CONNECTOR;
import static com.tle.common.connectors.ConnectorConstants.PRIV_EDIT_CONNECTOR;

import javax.inject.Inject;

import com.tle.common.Pair;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.settings.AbstractParentSettingsSection;

@Bind
@SuppressWarnings("nls")
public class ConnectorsSettingsSection extends AbstractParentSettingsSection<Object>
{
	@PlugKey("connector.setting.title")
	private static Label TITLE_LABEL;
	@PlugKey("connector.setting.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private TLEAclManager aclService;

	@Override
	public boolean canView(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(PRIV_CREATE_CONNECTOR, PRIV_EDIT_CONNECTOR).isEmpty();
	}

	@Override
	public Pair<HtmlLinkState, Label> getLink(RenderEventContext context)
	{
		return new Pair<HtmlLinkState, Label>(getShowConnectorsLink(context), DESCRIPTION_LABEL);
	}

	public static HtmlLinkState getShowConnectorsLink(SectionInfo info)
	{
		final HtmlLinkState state = new HtmlLinkState();
		final SectionInfo fwd = info.createForward("/access/connectors.do");
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return state;
	}
}
