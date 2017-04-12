package com.tle.web.payment.section.catalogue;

import java.util.Collection;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.common.payment.entity.Catalogue;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.payment.service.session.CatalogueEditingSession.CatalogueEditingBean;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractEntityContributeSection;
import com.tle.web.entities.section.EntityEditor;
import com.tle.web.payment.section.catalogue.CatalogueContributeSection.CatalogueContributeModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

@TreeIndexed
public class CatalogueContributeSection
	extends
		AbstractEntityContributeSection<CatalogueEditingBean, Catalogue, CatalogueContributeModel>
{
	@PlugKey("catalogue.title.creating")
	private static Label LABEL_CREATING;
	@PlugKey("catalogue.title.editing")
	private static Label LABEL_EDITING;

	@Inject
	private CatalogueEditorSection catalogueEditorSection;
	@Inject
	private CatalogueService catalogueService;

	@Override
	protected AbstractEntityService<CatalogueEditingBean, Catalogue> getEntityService()
	{
		return catalogueService;
	}

	@Override
	protected Collection<EntityEditor<CatalogueEditingBean, Catalogue>> getAllEditors()
	{
		return Lists.newArrayList((EntityEditor<CatalogueEditingBean, Catalogue>) catalogueEditorSection);
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
	protected EntityEditor<CatalogueEditingBean, Catalogue> getEditor(SectionInfo info)
	{
		return catalogueEditorSection;
	}

	@Override
	protected String getCreatePriv()
	{
		return PaymentConstants.PRIV_CREATE_CATALOGUE;
	}

	@Override
	protected String getEditPriv()
	{
		return PaymentConstants.PRIV_EDIT_CATALOGUE;
	}

	public class CatalogueContributeModel
		extends
			AbstractEntityContributeSection<CatalogueEditingBean, Catalogue, CatalogueContributeModel>.EntityContributeModel
	{

	}
}
