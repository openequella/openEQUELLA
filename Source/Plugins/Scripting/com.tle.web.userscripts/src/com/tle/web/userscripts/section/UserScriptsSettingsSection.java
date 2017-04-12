package com.tle.web.userscripts.section;

import javax.inject.Inject;

import com.tle.common.userscripts.UserScriptsConstants;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.web.entities.section.AbstractEntitySettingsLinkSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@Bind
public class UserScriptsSettingsSection extends AbstractEntitySettingsLinkSection<Object>
{
	@PlugKey("settings.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private TLEAclManager aclManager;

	@Override
	public boolean canView(SectionInfo info)
	{
		return !aclManager.filterNonGrantedPrivileges(UserScriptsConstants.PRIV_CREATE_SCRIPT,
			UserScriptsConstants.PRIV_EDIT_SCRIPT).isEmpty();
	}

	public static HtmlLinkState getShowScriptsLink(SectionInfo info)
	{
		final HtmlLinkState state = new HtmlLinkState();
		final SectionInfo fwd = info.createForward("/access/userscripts.do"); //$NON-NLS-1$
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return state;

	}

	@Override
	protected HtmlLinkState getShowEntitiesLink(SectionInfo info)
	{
		return getShowScriptsLink(info);
	}

	@Override
	protected Label getDescriptionLabel(SectionInfo info)
	{
		return DESCRIPTION_LABEL;
	}

}
