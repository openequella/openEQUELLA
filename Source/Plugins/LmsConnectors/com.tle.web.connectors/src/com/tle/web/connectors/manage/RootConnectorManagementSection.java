package com.tle.web.connectors.manage;

import javax.inject.Inject;

import com.tle.web.connectors.service.ConnectorManagementPrivilegeTreeProvider;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;

public class RootConnectorManagementSection extends ContextableSearchSection<ContextableSearchSection.Model>
{
	@PlugKey("manage.title")
	private static Label LABEL_TITLE;
	@Inject
	private ConnectorManagementPrivilegeTreeProvider securityProvider;

	private static final String SESSION_KEY = "connectorKey"; //$NON-NLS-1$

	@Override
	protected String getSessionKey()
	{
		return SESSION_KEY;
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return LABEL_TITLE;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		securityProvider.checkAuthorised();
		return super.renderHtml(context);
	}

}
