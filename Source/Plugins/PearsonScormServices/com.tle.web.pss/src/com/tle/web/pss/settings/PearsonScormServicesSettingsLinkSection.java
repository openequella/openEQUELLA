package com.tle.web.pss.settings;

import javax.inject.Inject;

import com.tle.common.Pair;
import com.tle.core.guice.Bind;
import com.tle.core.pss.util.PSSConstants;
import com.tle.web.pss.privileges.PearsonScormServicesSettingsPrivilegeTreeProvider;
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
public class PearsonScormServicesSettingsLinkSection extends AbstractParentSettingsSection<Object>
{
	@PlugKey("settings.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private PearsonScormServicesSettingsPrivilegeTreeProvider securityProvider;

	@Override
	public boolean canView(SectionInfo info)
	{
		return securityProvider.isAuthorised();
	}

	@Override
	public Pair<HtmlLinkState, Label> getLink(RenderEventContext context)
	{
		return new Pair<HtmlLinkState, Label>(getShowPSSLink(context), getDescriptionLabel(context));
	}

	public static HtmlLinkState getShowPSSLink(SectionInfo info)
	{
		final HtmlLinkState state = new HtmlLinkState();
		final SectionInfo fwd = info.createForward("/access/psssettings.do");
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return state;
	}

	protected Label getDescriptionLabel(SectionInfo info)
	{
		return DESCRIPTION_LABEL;
	}
}