package com.tle.web.portal.section.admin;

import javax.inject.Inject;

import com.tle.common.Pair;
import com.tle.core.guice.Bind;
import com.tle.web.portal.section.common.PortletContributionSection;
import com.tle.web.portal.service.PortletWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.settings.AbstractParentSettingsSection;

@Bind
public class PortalSettingsSection extends AbstractParentSettingsSection<Object>
{
	@PlugKey("setting.title")
	private static Label TITLE_LABEL;
	@PlugKey("setting.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private PortletWebService portletWebService;

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return portletWebService.canAdminister();
	}

	@Override
	@SuppressWarnings("nls")
	public Pair<HtmlLinkState, Label> getLink(RenderEventContext context)
	{
		HtmlLinkState state = new HtmlLinkState();
		SectionInfo fwd = context.createForward("/access/portaladmin.do");
		PortletContributionSection sec = fwd.lookupSection(PortletContributionSection.class);
		sec.setFromSettings(fwd, true);
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return new Pair<HtmlLinkState, Label>(state, DESCRIPTION_LABEL);
	}
}
