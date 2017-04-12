package com.tle.web.payment.section.storefront;

import javax.inject.Inject;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.payment.entity.StoreFront;
import com.tle.core.payment.service.StoreFrontService;
import com.tle.core.payment.service.session.StoreFrontEditingBean;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractShowEntitiesSection;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.js.JSValidator;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.validators.SimpleValidator;
import com.tle.web.sections.render.Label;

public class ShowStoreFrontsSection
	extends
		AbstractShowEntitiesSection<StoreFront, AbstractShowEntitiesSection.AbstractShowEntitiesModel>
{
	private static PluginResourceHelper helper = ResourcesService.getResourceHelper(ShowStoreFrontsSection.class);
	private static final String STOREFRONT_IN_USE_MESSAGE = helper.key("storefront.showlist.delete.storefrontinuse"); //$NON-NLS-1$

	@PlugKey("storefront.setting.title")
	private static Label LABEL_STOREFRONTS;
	@PlugKey("storefront.showlist.empty")
	private static Label LABEL_EMPTY_LIST;
	@PlugKey("storefront.showlist.delete.confirm")
	private static Label LABEL_DELETE_CONFIRM;
	@PlugKey("storefront.showlist.column.storefront")
	private static Label LABEL_COLUMN_STOREFRONT;
	@PlugKey("storefront.showlist.link.addnew")
	private static Label LABEL_ADD_STOREFRONT;

	@Inject
	private StoreFrontService storeFrontService;

	@Override
	protected boolean canAdd(SectionInfo info)
	{
		return storeFrontService.canCreate();
	}

	@Override
	protected AbstractEntityService<StoreFrontEditingBean, StoreFront> getEntityService()
	{
		return storeFrontService;
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return LABEL_STOREFRONTS;
	}

	@Override
	protected Label getAddLabel()
	{
		return LABEL_ADD_STOREFRONT;
	}

	@Override
	protected Label getEntityColumnLabel()
	{
		return LABEL_COLUMN_STOREFRONT;
	}

	@Override
	protected Label getEmptyListLabel()
	{
		return LABEL_EMPTY_LIST;
	}

	@Override
	protected Label getDeleteConfirmLabel(SectionInfo info, StoreFront storeFront)
	{
		return LABEL_DELETE_CONFIRM;
	}

	@Override
	protected JSValidator getDeleteValidator(SectionInfo info, StoreFront storeFront)
	{
		boolean storeFrontHasHistory = storeFrontService.storeFrontHasHistory(storeFront);
		// @formatter:off
		return storeFrontHasHistory ?
			new SimpleValidator(Js.alert(CurrentLocale.get(STOREFRONT_IN_USE_MESSAGE))) :
				super.getDeleteValidator(info, storeFront);
		// @formatter:on
	}

	@Override
	protected boolean canClone(SectionInfo info, StoreFront ent)
	{
		return false;
	}

	@Override
	protected boolean isInUse(SectionInfo info, StoreFront entity)
	{
		return false;
	}
}
