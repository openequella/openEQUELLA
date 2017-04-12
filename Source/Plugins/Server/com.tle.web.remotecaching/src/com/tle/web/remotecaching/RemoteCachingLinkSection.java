package com.tle.web.remotecaching;

import javax.inject.Inject;

import com.tle.common.Pair;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.settings.AbstractParentSettingsSection;

/**
 * @author larry
 */
@Bind
@SuppressWarnings("nls")
public class RemoteCachingLinkSection extends AbstractParentSettingsSection<Object>
{
	@PlugKey("remotecaching.title")
	private static Label LINK_TITLE_LABEL;

	@PlugKey("remotecaching.description")
	private static Label DESCRIPTION_LABEL;
	@Inject
	private RemoteCachingPrivilegeTreeProvider securityProvider;

	@Override
	public boolean canView(SectionInfo info)
	{
		return securityProvider.isAuthorised();
	}

	@Override
	public Pair<HtmlLinkState, Label> getLink(RenderEventContext context)
	{
		HtmlLinkState state = new HtmlLinkState();
		SectionInfo fwd = context.createForward("/access/remotecaching.do");
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(LINK_TITLE_LABEL);
		return new Pair<HtmlLinkState, Label>(state, DESCRIPTION_LABEL);
	}

}
