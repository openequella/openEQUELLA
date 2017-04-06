package com.tle.web.search.settings;

import javax.inject.Inject;

import com.tle.common.Pair;
import com.tle.core.guice.Bind;
import com.tle.core.search.service.impl.SearchPrivilegeTreeProvider;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.settings.AbstractParentSettingsSection;

/**
 * @author Aaron
 */
@Bind
public class SearchSettingsLinksSection extends AbstractParentSettingsSection<Object>
{
	@PlugKey("settings.link.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.link.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private SearchPrivilegeTreeProvider securityProvider;

	@Override
	public boolean canView(SectionInfo info)
	{
		return securityProvider.isAuthorised();
	}

	@Override
	@SuppressWarnings("nls")
	public Pair<HtmlLinkState, Label> getLink(RenderEventContext context)
	{
		HtmlLinkState state = new HtmlLinkState();
		SectionInfo fwd = context.createForward("/access/searchsettings.do");
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return new Pair<HtmlLinkState, Label>(state, DESCRIPTION_LABEL);
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}
}
