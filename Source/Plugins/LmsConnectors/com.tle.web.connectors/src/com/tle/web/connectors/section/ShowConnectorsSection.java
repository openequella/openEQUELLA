package com.tle.web.connectors.section;

import javax.inject.Inject;

import com.tle.common.connectors.ConnectorConstants;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.service.ConnectorEditingBean;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractShowEntitiesSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

@Bind
public class ShowConnectorsSection
	extends
		AbstractShowEntitiesSection<Connector, ShowConnectorsSection.ShowConnectorsSectionModel>
{
	@PlugKey("connectors.page.title")
	private static Label LABEL_PAGE_TITLE;
	@PlugKey("connectors.link.add")
	private static Label LABEL_LINK_ADD;
	@PlugKey("connectors.emptylist")
	private static Label LABEL_EMPTY_LIST;
	@PlugKey("connectors.confirm.delete")
	private static Label LABEL_DELETE_CONFIRM;
	@PlugKey("connectors.column.connector")
	private static Label LABEL_CONNECTOR;

	@Inject
	private ConnectorService connectorService;
	@Inject
	private TLEAclManager aclService;

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return LABEL_PAGE_TITLE;
	}

	@Override
	protected AbstractEntityService<ConnectorEditingBean, Connector> getEntityService()
	{
		return connectorService;
	}

	@Override
	protected Label getAddLabel()
	{
		return LABEL_LINK_ADD;
	}

	@Override
	protected Label getEntityColumnLabel()
	{
		return LABEL_CONNECTOR;
	}

	@Override
	protected Label getEmptyListLabel()
	{
		return LABEL_EMPTY_LIST;
	}

	@Override
	protected Label getDeleteConfirmLabel(SectionInfo info, Connector connector)
	{
		return LABEL_DELETE_CONFIRM;
	}

	@Override
	protected boolean canAdd(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(ConnectorConstants.PRIV_CREATE_CONNECTOR).isEmpty();
	}

	@Override
	protected boolean isInUse(SectionInfo info, Connector entity)
	{
		return false;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ShowConnectorsSectionModel();
	}

	public class ShowConnectorsSectionModel extends AbstractShowEntitiesSection.AbstractShowEntitiesModel
	{

	}
}
