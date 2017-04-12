package com.tle.web.connectors.viewitem;

import com.google.inject.Inject;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.common.connectors.ConnectorConstants;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.summary.sidebar.actions.GenericMinorActionWithPageSection;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class FindUsesLinkSection extends GenericMinorActionWithPageSection
{
	@PlugKey("link.title")
	private static Label LINK_LABEL;

	@TreeLookup
	private FindUsesContentSection findUsesContentSection;
	@Inject
	private ConnectorService connectorService;

	@Override
	protected Label getLinkLabel()
	{
		return LINK_LABEL;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return !connectorService.listForViewing().isEmpty()
			&& itemInfo.hasPrivilege(ConnectorConstants.PRIV_FIND_USES_ITEM);
	}

	@Override
	protected SectionId getPageSection()
	{
		return findUsesContentSection;
	}

	@Override
	public String getLinkText()
	{
		return LINK_LABEL.getText();
	}
}
