package com.tle.web.connectors.section;

import static com.tle.common.connectors.ConnectorConstants.PRIV_CREATE_CONNECTOR;
import static com.tle.common.connectors.ConnectorConstants.PRIV_EDIT_CONNECTOR;

import java.util.Arrays;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.web.entities.section.AbstractRootEntitySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout.OneColumnLayoutModel;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@Bind
public class RootConnectorsSection extends AbstractRootEntitySection<OneColumnLayoutModel>
{
	@PlugKey("connectors.page.title")
	private static Label TITLE_LABEL;

	@Inject
	private TLEAclManager aclService;

	@Override
	public Class<OneColumnLayoutModel> getModelClass()
	{
		return OneColumnLayoutModel.class;
	}

	@Override
	protected boolean canView(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(Arrays.asList(PRIV_EDIT_CONNECTOR, PRIV_CREATE_CONNECTOR))
			.isEmpty();
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return TITLE_LABEL;
	}

	@Override
	protected HtmlLinkState getShowEntitiesLink(SectionInfo info)
	{
		return ConnectorsSettingsSection.getShowConnectorsLink(info);
	}
}
