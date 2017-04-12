package com.tle.web.userscripts.section;

import javax.inject.Inject;

import com.tle.common.userscripts.UserScriptsConstants;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.web.entities.section.AbstractRootEntitySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout.OneColumnLayoutModel;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@Bind
public class RootUserScriptsSection extends AbstractRootEntitySection<OneColumnLayoutModel>
{

	@PlugKey("settings.title")
	private static Label TITLE_LABEL;
	
	@Inject
	private TLEAclManager aclManager;

	@Override
	protected boolean canView(SectionInfo info)
	{
		return !aclManager.filterNonGrantedPrivileges(UserScriptsConstants.PRIV_CREATE_SCRIPT,
			UserScriptsConstants.PRIV_EDIT_SCRIPT).isEmpty();
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return TITLE_LABEL;
	}

	@Override
	protected HtmlLinkState getShowEntitiesLink(SectionInfo info)
	{
		return UserScriptsSettingsSection.getShowScriptsLink(info);
	}

}
