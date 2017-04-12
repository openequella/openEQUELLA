package com.tle.web.payment.section.tier;

import static com.tle.core.payment.PaymentConstants.PRIV_CREATE_TIER;
import static com.tle.core.payment.PaymentConstants.PRIV_EDIT_TIER;

import java.util.Collection;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.common.payment.entity.PricingTier;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.payment.service.session.PricingTierEditingBean;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractEntityContributeSection;
import com.tle.web.entities.section.EntityEditor;
import com.tle.web.payment.section.tier.TierContributeSection.TierContributeModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

@TreeIndexed
public class TierContributeSection
	extends
		AbstractEntityContributeSection<PricingTierEditingBean, PricingTier, TierContributeModel>
{
	@PlugKey("tier.purchase.creating")
	private static Label LABEL_PURCHASE_CREATING;
	@PlugKey("tier.sub.creating")
	private static Label LABEL_SUB_CREATING;
	@PlugKey("tier.purchase.editing")
	private static Label LABEL_PURCHASE_EDITING;
	@PlugKey("tier.sub.editing")
	private static Label LABEL_SUB_EDITING;

	@Inject
	private PricingTierEditorSection tierEditorSection;
	@Inject
	private PricingTierService tierService;

	@Override
	protected AbstractEntityService<PricingTierEditingBean, PricingTier> getEntityService()
	{
		return tierService;
	}

	@Override
	public void createNew(SectionInfo info)
	{
		throw new Error("Please call createNew with purchase or subscription param");
	}

	public void createNew(SectionInfo info, boolean purchase)
	{
		final TierContributeModel model = getModel(info);
		model.setEditing(true);

		final PricingTierEditorSection ed = (PricingTierEditorSection) getEditor(info);
		model.setEditor(ed);
		if( ed != null )
		{
			ed.setPurchase(info, purchase);
			ed.create(info);
		}
	}

	@Override
	protected Collection<EntityEditor<PricingTierEditingBean, PricingTier>> getAllEditors()
	{
		return Lists.newArrayList((EntityEditor<PricingTierEditingBean, PricingTier>) tierEditorSection);
	}

	@Override
	protected Label getCreatingLabel(SectionInfo info)
	{
		if( tierEditorSection.getModel(info).isPurchase() )
		{
			return LABEL_PURCHASE_CREATING;
		}
		else
		{
			return LABEL_SUB_CREATING;
		}
	}

	@Override
	protected Label getEditingLabel(SectionInfo info)
	{
		if( tierEditorSection.getModel(info).isPurchase() )
		{
			return LABEL_PURCHASE_EDITING;
		}
		else
		{
			return LABEL_SUB_EDITING;
		}
	}

	@Override
	protected EntityEditor<PricingTierEditingBean, PricingTier> getEditor(SectionInfo info)
	{
		return tierEditorSection;
	}

	@Override
	protected String getCreatePriv()
	{
		return PRIV_CREATE_TIER;
	}

	@Override
	protected String getEditPriv()
	{
		return PRIV_EDIT_TIER;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new TierContributeModel();
	}

	public class TierContributeModel
		extends
			AbstractEntityContributeSection<PricingTierEditingBean, PricingTier, TierContributeModel>.EntityContributeModel
	{

	}
}
