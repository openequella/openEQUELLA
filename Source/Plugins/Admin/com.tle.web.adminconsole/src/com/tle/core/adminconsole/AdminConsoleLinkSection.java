package com.tle.core.adminconsole;

import javax.inject.Inject;

import com.tle.common.Pair;
import com.tle.common.adminconsole.RemoteAdminService;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.settings.AbstractParentSettingsSection;

@SuppressWarnings("nls")
@Bind
public class AdminConsoleLinkSection extends AbstractParentSettingsSection<Object>
{
	@PlugKey("admin.link.title")
	private static Label TITLE_LABEL;
	@PlugKey("admin.link.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private RemoteAdminService adminService;

	@Override
	public boolean canView(SectionInfo info)
	{
		return !adminService.getAllowedTools().isEmpty();
	}

	@Override
	public Pair<HtmlLinkState, Label> getLink(RenderEventContext context)
	{
		HtmlLinkState state = new HtmlLinkState();
		state.setBookmark(new SimpleBookmark("jnlp/admin.jnlp"));
		state.setLabel(TITLE_LABEL);
		state.setTarget("_blank");
		return new Pair<HtmlLinkState, Label>(state, DESCRIPTION_LABEL);
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}
}
