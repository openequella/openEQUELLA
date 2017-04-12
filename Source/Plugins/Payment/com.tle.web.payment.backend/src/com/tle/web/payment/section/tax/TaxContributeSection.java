package com.tle.web.payment.section.tax;

import static com.tle.core.payment.PaymentConstants.PRIV_CREATE_TAX;
import static com.tle.core.payment.PaymentConstants.PRIV_EDIT_TAX;

import java.util.Collection;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.common.payment.entity.TaxType;
import com.tle.core.payment.service.TaxService;
import com.tle.core.payment.service.session.TaxTypeEditingBean;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractEntityContributeSection;
import com.tle.web.entities.section.EntityEditor;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

@TreeIndexed
public class TaxContributeSection
	extends
		AbstractEntityContributeSection<TaxTypeEditingBean, TaxType, TaxContributeSection.TaxContributeModel>
{
	@PlugKey("tax.creating")
	private static Label LABEL_CREATING;
	@PlugKey("tax.editing")
	private static Label LABEL_EDITING;

	@Inject
	private TaxEditorSection taxEditorSection;
	@Inject
	private TaxService taxService;

	@Override
	protected AbstractEntityService<TaxTypeEditingBean, TaxType> getEntityService()
	{
		return taxService;
	}

	@Override
	protected Collection<EntityEditor<TaxTypeEditingBean, TaxType>> getAllEditors()
	{
		return Lists.newArrayList((EntityEditor<TaxTypeEditingBean, TaxType>) taxEditorSection);
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
	protected EntityEditor<TaxTypeEditingBean, TaxType> getEditor(SectionInfo info)
	{
		return taxEditorSection;
	}

	@Override
	protected String getCreatePriv()
	{
		return PRIV_CREATE_TAX;
	}

	@Override
	protected String getEditPriv()
	{
		return PRIV_EDIT_TAX;
	}

	public class TaxContributeModel
		extends
			AbstractEntityContributeSection<TaxTypeEditingBean, TaxType, TaxContributeModel>.EntityContributeModel
	{

	}
}
