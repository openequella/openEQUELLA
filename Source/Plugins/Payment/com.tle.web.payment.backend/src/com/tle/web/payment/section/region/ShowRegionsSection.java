package com.tle.web.payment.section.region;

import static com.tle.core.payment.PaymentConstants.PRIV_CREATE_REGION;

import java.util.List;

import javax.inject.Inject;

import com.tle.common.payment.entity.Region;
import com.tle.core.payment.service.RegionService;
import com.tle.core.payment.service.session.RegionEditingBean;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractShowEntitiesSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

public class ShowRegionsSection
	extends
		AbstractShowEntitiesSection<Region, AbstractShowEntitiesSection.AbstractShowEntitiesModel>
{
	@PlugKey("region.showlist.link.add")
	private static Label LABEL_LINK_ADD;
	@PlugKey("region.showlist.title")
	private static Label LABEL_REGIONS;
	@PlugKey("region.showlist.column.title")
	private static Label LABEL_REGION;
	@PlugKey("region.showlist.empty")
	private static Label LABEL_EMPTY_LIST;
	@PlugKey("region.showlist.delete.confirm")
	private static Label LABEL_DELETE_CONFIRM;
	@PlugKey("region.showlist.inuse")
	private static Label LABEL_IN_USE;

	@Inject
	private TLEAclManager aclService;

	@Inject
	private RegionService regionService;

	@Override
	protected boolean canAdd(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(PRIV_CREATE_REGION).isEmpty();
	}

	@Override
	protected AbstractEntityService<RegionEditingBean, Region> getEntityService()
	{
		return regionService;
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return LABEL_REGIONS;
	}

	@Override
	protected Label getAddLabel()
	{
		return LABEL_LINK_ADD;
	}

	@Override
	protected Label getEntityColumnLabel()
	{
		return LABEL_REGION;
	}

	@Override
	protected Label getEmptyListLabel()
	{
		return LABEL_EMPTY_LIST;
	}

	@Override
	protected Label getDeleteConfirmLabel(SectionInfo info, Region region)
	{
		return LABEL_DELETE_CONFIRM;
	}

	@Override
	protected boolean isInUse(SectionInfo info, Region entity)
	{
		final List<Class<?>> references = regionService.getReferencingClasses(entity.getId());
		return !references.isEmpty();
	}

	@Override
	protected Label getInUseLabel(SectionInfo info, Region entity)
	{
		return LABEL_IN_USE;
	}
}
