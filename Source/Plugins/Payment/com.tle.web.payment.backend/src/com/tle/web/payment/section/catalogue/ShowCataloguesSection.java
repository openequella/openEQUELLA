package com.tle.web.payment.section.catalogue;

import javax.inject.Inject;

import com.tle.common.payment.entity.Catalogue;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.payment.service.session.CatalogueEditingSession.CatalogueEditingBean;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractShowEntitiesSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

public class ShowCataloguesSection
	extends
		AbstractShowEntitiesSection<Catalogue, AbstractShowEntitiesSection.AbstractShowEntitiesModel>
{
	@PlugKey("catalogue.showlist.link.add")
	private static Label LABEL_LINK_ADD;
	@PlugKey("catalogue.showlist.page.title")
	private static Label LABEL_CATALOGUES;
	@PlugKey("catalogue.showlist.column.catalogue")
	private static Label LABEL_COLUMN_CATALOGUE;
	@PlugKey("catalogue.showlist.empty")
	private static Label LABEL_EMPTY_LIST;
	@PlugKey("catalogue.showlist.delete.confirm")
	private static Label LABEL_DELETE_CONFIRM;

	@Inject
	private CatalogueService catalogueService;
	@Inject
	private TLEAclManager aclService;

	@Override
	protected boolean canAdd(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(PaymentConstants.PRIV_CREATE_CATALOGUE).isEmpty();
	}

	@Override
	protected AbstractEntityService<CatalogueEditingBean, Catalogue> getEntityService()
	{
		return catalogueService;
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return LABEL_CATALOGUES;
	}

	@Override
	protected Label getAddLabel()
	{
		return LABEL_LINK_ADD;
	}

	@Override
	protected Label getEntityColumnLabel()
	{
		return LABEL_COLUMN_CATALOGUE;
	}

	@Override
	protected Label getEmptyListLabel()
	{
		return LABEL_EMPTY_LIST;
	}

	@Override
	protected Label getDeleteConfirmLabel(SectionInfo info, Catalogue cat)
	{
		return LABEL_DELETE_CONFIRM;
	}

	@Override
	protected boolean isInUse(SectionInfo info, Catalogue entity)
	{
		return false;
	}

	@Override
	protected void doDeleteEntity(SectionInfo info, String uuid)
	{
		AbstractEntityService<CatalogueEditingBean, Catalogue> entityService = getEntityService();
		entityService.delete(entityService.getByUuid(uuid), false);
	}
}
