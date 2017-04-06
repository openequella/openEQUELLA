package com.tle.web.payment.section.tier;

import static com.tle.core.payment.PaymentConstants.PRIV_CREATE_TIER;

import java.util.List;

import javax.inject.Inject;

import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.core.payment.PaymentSettings;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.payment.service.session.PricingTierEditingBean;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractShowEntitiesSection;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.payment.service.PaymentWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

@SuppressWarnings("nls")
public abstract class AbstractShowTiersSection
	extends
		AbstractShowEntitiesSection<PricingTier, AbstractShowTiersSection.AbstractShowTiersModel>
{
	@PlugKey("tier.showlist.column.tier")
	private static Label LABEL_COLUMN_TIER;
	@PlugKey("tier.showlist.delete.confirm")
	private static Label LABEL_DELETE_CONFIRM;
	@PlugKey("tier.showlist.pricesbasedon.flatrate")
	private static String KEY_FLAT_RATE;
	@PlugKey("tier.showlist.pricesbasedon.peruser")
	private static String KEY_PER_USER;
	@PlugKey("tier.showlist.alert.tierinuse")
	private static Label LABEL_TIER_IN_USE;

	@Inject
	private TLEAclManager aclService;
	@Inject
	private PricingTierService tierService;
	@Inject
	private PaymentWebService paymentWebService;

	@Component(name = "fr", stateful = false)
	private SingleSelectionList<VoidKeyOption> flatRate;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;

	private final boolean purchase;

	protected AbstractShowTiersSection(boolean purchase)
	{
		this.purchase = purchase;
	}

	@Override
	public SectionRenderable renderTop(RenderEventContext context)
	{
		final PaymentSettings settings = paymentWebService.getSettings(context);
		boolean fr = (purchase ? settings.isPurchaseFlatRate() : settings.isSubscriptionFlatRate());
		flatRate.setSelectedStringValue(context, Boolean.toString(fr));

		return view.createResult("pricesbasedon.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		flatRate.setListModel(new SimpleHtmlListModel<VoidKeyOption>(new VoidKeyOption(KEY_FLAT_RATE, Boolean.TRUE
			.toString()), new VoidKeyOption(KEY_PER_USER, Boolean.FALSE.toString())));
		// Is actually not a dom updating function
		flatRate.addChangeEventHandler(new OverrideHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("update"), "NOTHING")));
	}

	@EventHandlerMethod
	public void update(SectionInfo info)
	{
		final PaymentSettings settings = paymentWebService.getSettings(info);
		final boolean fr = Boolean.parseBoolean(flatRate.getSelectedValueAsString(info));
		if( purchase )
		{
			settings.setPurchaseFlatRate(fr);
		}
		else
		{
			settings.setSubscriptionFlatRate(fr);
		}
		paymentWebService.saveSettings(info, settings);
	}

	@Override
	protected void createNew(SectionInfo info)
	{
		((TierContributeSection) getContribSection(info)).createNew(info, purchase);
	}

	@Override
	protected Label getEntityColumnLabel()
	{
		return LABEL_COLUMN_TIER;
	}

	@Override
	protected Label getDeleteConfirmLabel(SectionInfo info, PricingTier tier)
	{
		return LABEL_DELETE_CONFIRM;
	}

	@Override
	protected boolean canAdd(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(PRIV_CREATE_TIER).isEmpty();
	}

	@Override
	protected AbstractEntityService<PricingTierEditingBean, PricingTier> getEntityService()
	{
		return tierService;
	}

	@Override
	protected List<PricingTier> getEntityList(SectionInfo info)
	{
		return tierService.enumerateEditable(purchase);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new AbstractShowTiersModel();
	}

	public SingleSelectionList<VoidKeyOption> getFlatRate()
	{
		return flatRate;
	}

	@Override
	protected boolean isInUse(SectionInfo info, PricingTier tier)
	{
		final List<PricingTierAssignment> tiers = tierService.listAssignmentsForTier(tier);
		return tiers.size() > 0;
	}

	@Override
	protected Label getInUseLabel(SectionInfo info, PricingTier entity)
	{
		return LABEL_TIER_IN_USE;
	}

	public class AbstractShowTiersModel extends AbstractShowEntitiesSection.AbstractShowEntitiesModel
	{
		// Nothing else
	}
}
