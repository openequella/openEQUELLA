package com.tle.web.lti.consumers.section;

import javax.inject.Inject;

import com.tle.common.lti.consumers.LtiConsumerConstants;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.core.lti.consumers.service.LtiConsumerService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractShowEntitiesSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

public class ShowLtiConsumersSection
	extends
		AbstractShowEntitiesSection<LtiConsumer, ShowLtiConsumersSection.ShowLtiConsumersModel>
{
	@PlugKey("settings.title")
	private static Label TITLE_LABEL;
	@PlugKey("consumers.add")
	private static Label ADD_CONSUMER_LABEL;
	@PlugKey("consumers.column.consumers")
	private static Label ENTITY_COLUMN_LABEL;
	@PlugKey("consumers.table.empty")
	private static Label EMPTY_LABEL;
	@PlugKey("consumers.confirm.delete")
	private static Label DELETE_CONFIRM_LABEL;

	@Inject
	private LtiConsumerService ltiConsumerService;
	@Inject
	private TLEAclManager aclManager;

	@Override
	protected AbstractEntityService<?, LtiConsumer> getEntityService()
	{
		return ltiConsumerService;
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return TITLE_LABEL;
	}

	@Override
	protected Label getAddLabel()
	{
		return ADD_CONSUMER_LABEL;
	}

	@Override
	protected Label getEntityColumnLabel()
	{
		return ENTITY_COLUMN_LABEL;
	}

	@Override
	protected Label getEmptyListLabel()
	{
		return EMPTY_LABEL;
	}

	@Override
	protected Label getDeleteConfirmLabel(SectionInfo info, LtiConsumer entity)
	{
		return DELETE_CONFIRM_LABEL;
	}

	@Override
	protected boolean canAdd(SectionInfo info)
	{
		return !aclManager.filterNonGrantedPrivileges(LtiConsumerConstants.PRIV_CREATE_CONUSMER).isEmpty();
	}

	@Override
	protected boolean isInUse(SectionInfo info, LtiConsumer entity)
	{
		return false;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ShowLtiConsumersModel();
	}

	public class ShowLtiConsumersModel extends AbstractShowEntitiesSection.AbstractShowEntitiesModel
	{
		// nothing
	}

}
