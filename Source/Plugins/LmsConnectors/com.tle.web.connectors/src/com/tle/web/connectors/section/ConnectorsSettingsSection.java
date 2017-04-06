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
	@PlugKey("setting.title")
	private static Label TITLE_LABEL;
	@PlugKey("setting.description")
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
