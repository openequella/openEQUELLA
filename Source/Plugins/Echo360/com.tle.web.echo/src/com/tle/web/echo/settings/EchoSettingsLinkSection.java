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
	@PlugKey("settings.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.description")
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
