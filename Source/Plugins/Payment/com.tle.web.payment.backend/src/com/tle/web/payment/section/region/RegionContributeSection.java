package com.tle.web.payment.section.region;

import static com.tle.core.payment.PaymentConstants.PRIV_CREATE_REGION;
import static com.tle.core.payment.PaymentConstants.PRIV_EDIT_REGION;

import java.util.Collection;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.common.payment.entity.Region;
import com.tle.core.payment.service.RegionService;
import com.tle.core.payment.service.session.RegionEditingBean;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractEntityContributeSection;
import com.tle.web.entities.section.EntityEditor;
import com.tle.web.payment.section.region.RegionContributeSection.RegionContributeModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

@TreeIndexed
public class RegionContributeSection
	extends
		AbstractEntityContributeSection<RegionEditingBean, Region, RegionContributeModel>
{
	@PlugKey("region.creating")
	private static Label LABEL_CREATING;
	@PlugKey("region.editing")
	private static Label LABEL_EDITING;

	@Inject
	private RegionEditorSection regionEditorSection;
	@Inject
	private RegionService regionService;

	@Override
	protected AbstractEntityService<RegionEditingBean, Region> getEntityService()
	{
		return regionService;
	}

	@Override
	protected Collection<EntityEditor<RegionEditingBean, Region>> getAllEditors()
	{
		return Lists.newArrayList((EntityEditor<RegionEditingBean, Region>) regionEditorSection);
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
	protected EntityEditor<RegionEditingBean, Region> getEditor(SectionInfo info)
	{
		return regionEditorSection;
	}

	@Override
	protected String getCreatePriv()
	{
		return PRIV_CREATE_REGION;
	}

	@Override
	protected String getEditPriv()
	{
		return PRIV_EDIT_REGION;
	}

	public class RegionContributeModel
		extends
			AbstractEntityContributeSection<RegionEditingBean, Region, RegionContributeModel>.EntityContributeModel
	{

	}
}
