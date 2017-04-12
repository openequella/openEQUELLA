package com.tle.web.payment.storefront.section.store;

import java.util.Collection;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.payment.storefront.service.StoreService;
import com.tle.core.payment.storefront.service.session.StoreEditingBean;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractEntityContributeSection;
import com.tle.web.entities.section.EntityEditor;
import com.tle.web.payment.storefront.section.store.StoreContributeSection.StoreContributeModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

/**
 * This should not exist. It's only here for the TreeLookup in
 * AbstractShowEntities...
 * 
 * @author Aaron
 */
@TreeIndexed
public class StoreContributeSection
	extends
		AbstractEntityContributeSection<StoreEditingBean, Store, StoreContributeModel>
{
	@PlugKey("store.title.creating")
	private static Label LABEL_CREATING;
	@PlugKey("store.title.editing")
	private static Label LABEL_EDITING;

	@Inject
	private StoreService storeService;

	@Override
	protected AbstractEntityService<StoreEditingBean, Store> getEntityService()
	{
		return storeService;
	}

	@Override
	protected Label getCreatingLabel(SectionInfo info)
	{
		return LABEL_CREATING;
	}

	@Override
	protected Label getEditingLabel(SectionInfo info)
	{
		return LABEL_EDITING;
	}

	@Override
	protected EntityEditor<StoreEditingBean, Store> getEditor(SectionInfo info)
	{
		return null;
	}

	@Override
	protected String getCreatePriv()
	{
		return StoreFrontConstants.PRIV_CREATE_STORE;
	}

	@Override
	protected String getEditPriv()
	{
		return StoreFrontConstants.PRIV_EDIT_STORE;
	}

	@Override
	protected Collection<EntityEditor<StoreEditingBean, Store>> getAllEditors()
	{
		return Lists.newArrayList();
	}

	public class StoreContributeModel
		extends
			AbstractEntityContributeSection<StoreEditingBean, Store, StoreContributeModel>.EntityContributeModel
	{

	}
}
