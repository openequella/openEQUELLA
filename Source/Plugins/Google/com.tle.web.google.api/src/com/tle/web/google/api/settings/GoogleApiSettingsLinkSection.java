package com.tle.web.google.api.settings;

import javax.inject.Inject;

import com.tle.common.Pair;
import com.tle.core.guice.Bind;
import com.tle.web.google.api.privileges.GoogleApiSettingsPrivilegeTreeProvider;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.settings.AbstractParentSettingsSection;

@Bind
public class GoogleApiSettingsLinkSection extends AbstractParentSettingsSection<Object>
{
	@PlugKey("settings.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private GoogleApiSettingsPrivilegeTreeProvider securityProvider;

	@Override
	public boolean canView(SectionInfo info)
	{
		return securityProvider.isAuthorised();
	}

	@Override
	public Pair<HtmlLinkState, Label> getLink(RenderEventContext context)
	{
		final HtmlLinkState state = new HtmlLinkState();
		final SectionInfo fwd = context.createForward("/access/googleapisettings.do");
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return new Pair<HtmlLinkState, Label>(state, DESCRIPTION_LABEL);
	}

}
