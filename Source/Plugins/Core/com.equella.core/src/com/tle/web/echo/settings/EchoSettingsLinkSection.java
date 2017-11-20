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

package com.tle.web.echo.settings;

import javax.inject.Inject;

import com.tle.common.Pair;
import com.tle.core.echo.EchoConstants;
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
public class EchoSettingsLinkSection extends AbstractParentSettingsSection<Object>
{
	@PlugKey("echo.settings.title")
	private static Label TITLE_LABEL;
	@PlugKey("echo.settings.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private TLEAclManager aclService;

	@Override
	public boolean canView(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(EchoConstants.PRIV_CREATE_ECHO, EchoConstants.PRIV_EDIT_ECHO)
			.isEmpty();
	}

	@Override
	public Pair<HtmlLinkState, Label> getLink(RenderEventContext context)
	{
		return new Pair<HtmlLinkState, Label>(getShowEchoServersLink(context), DESCRIPTION_LABEL);
	}

	public static HtmlLinkState getShowEchoServersLink(SectionInfo info)
	{
		final HtmlLinkState state = new HtmlLinkState();
		final SectionInfo fwd = info.createForward("/access/echoservers.do");
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return state;
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}
}
