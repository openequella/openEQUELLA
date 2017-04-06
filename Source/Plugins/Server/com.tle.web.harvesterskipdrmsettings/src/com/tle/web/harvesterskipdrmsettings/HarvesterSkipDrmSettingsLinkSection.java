package com.tle.web.harvesterskipdrmsettings;

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
public class HarvesterSkipDrmSettingsLinkSection extends AbstractParentSettingsSection<Object>
{
	@PlugKey("harvesterskipdrmsettings.title")
	private static Label TITLE_LABEL;
	@PlugKey("harvesterskipdrmsettings.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private HarvesterSkipDrmSettingsPrivilegeTreeProvider securityProvider;

	@Override
	public boolean canView(SectionInfo info)
	{
		return securityProvider.isAuthorised();
	}

	@Override
	public Pair<HtmlLinkState, Label> getLink(RenderEventContext context)
	{
		HtmlLinkState state = new HtmlLinkState();
		SectionInfo fwd = context.createForward("/access/harvesterskipdrmsettings.do");
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
