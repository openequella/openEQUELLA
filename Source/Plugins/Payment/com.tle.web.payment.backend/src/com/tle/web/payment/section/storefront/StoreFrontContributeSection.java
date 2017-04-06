package com.tle.web.payment.section.storefront;

import static com.tle.core.payment.PaymentConstants.PRIV_EDIT_STOREFRONT;

import java.util.Collection;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.common.payment.entity.StoreFront;
import com.tle.core.payment.service.StoreFrontService;
import com.tle.core.payment.service.session.StoreFrontEditingBean;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractEntityContributeSection;
import com.tle.web.entities.section.EntityEditor;
import com.tle.web.payment.section.storefront.StoreFrontContributeSection.StoreFrontContributeModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

@TreeIndexed
public class StoreFrontContributeSection
	extends
		AbstractEntityContributeSection<StoreFrontEditingBean, StoreFront, StoreFrontContributeModel>
{
	@PlugKey("storefront.edit.creating")
	private static Label LABEL_CREATING;
	@PlugKey("storefront.edit.editing")
	private static Label LABEL_EDITING;

	@Inject
	private StoreFrontEditorSection storeFrontEditorSection;
	@Inject
	private StoreFrontService storeFrontService;

	@Override
	protected AbstractEntityService<StoreFrontEditingBean, StoreFront> getEntityService()
	{
		return storeFrontService;
	}

	@Override
	protected Collection<EntityEditor<StoreFrontEditingBean, StoreFront>> getAllEditors()
	{
		return Lists.newArrayList((EntityEditor<StoreFrontEditingBean, StoreFront>) storeFrontEditorSection);
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
	protected EntityEditor<StoreFrontEditingBean, StoreFront> getEditor(SectionInfo info)
	{
		return storeFrontEditorSection;
	}

	@Override
	protected String getCreatePriv()
	{
		// return PRIV_CREATE_STOREFRONT;
		return null;
	}

	@Override
	protected String getEditPriv()
	{
		return PRIV_EDIT_STOREFRONT;
	}

	public class StoreFrontContributeModel
		extends
			AbstractEntityContributeSection<StoreFrontEditingBean, StoreFront, StoreFrontContributeModel>.EntityContributeModel
	{

	}
}
