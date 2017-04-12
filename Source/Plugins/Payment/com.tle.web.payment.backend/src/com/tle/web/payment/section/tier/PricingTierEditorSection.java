package com.tle.web.payment.section.tier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.core.guice.Bind;
import com.tle.core.payment.service.PaymentService;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.payment.service.session.PricingTierEditingBean;
import com.tle.core.payment.service.session.PricingTierEditingBean.PriceBean;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.web.entities.section.AbstractEntityEditor;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.payment.section.tier.PricingTierEditorSection.TierEditorModel;
import com.tle.web.payment.service.PaymentWebService;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.MappedBooleans;
import com.tle.web.sections.standard.MappedStrings;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlValueState;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.SpanRenderer;
import com.tle.web.sections.standard.renderers.TextFieldRenderer;

@SuppressWarnings("nls")
@Bind
public class PricingTierEditorSection
	extends
		AbstractEntityEditor<PricingTierEditingBean, PricingTier, TierEditorModel>
{
	private static final String KEY_PURCHASE = "purchase";

	private static final IncludeFile JS_INCLUDE = new IncludeFile(ResourcesService.getResourceHelper(
		PricingTierEditorSection.class).url("scripts/edittier.js"));
	private static final ExternallyDefinedFunction PERIOD_ENABLED_FUNCTION = new ExternallyDefinedFunction(
		"periodEnabled", 2, JS_INCLUDE);

	@PlugKey("tier.edit.label.purchaseprice")
	private static Label LABEL_PURCHASE_PRICE;

	@Inject
	private PricingTierService tierService;
	@Inject
	private PaymentService paymentService;
	@Inject
	private PaymentWebService paymentWebService;
	@Inject
	private BundleCache bundleCache;

	@ViewFactory
	private FreemarkerFactory view;

	@Component(name = "spe", stateful = false)
	private MappedBooleans periodEnabled;
	@Component(name = "spp", stateful = false)
	private MappedStrings periodPrice;
	@Component(name = "e", stateful = false)
	private Checkbox enabled;

	@Override
	protected SectionRenderable renderFields(RenderEventContext context,
		EntityEditingSession<PricingTierEditingBean, PricingTier> session)
	{
		final PricingTierEditingBean tier = session.getBean();
		final TierEditorModel model = getModel(context);

		model.setPurchase(tier.isPurchase());

		final Label currencyLabel = new TextLabel(paymentWebService.getSettings(context).getCurrency());

		final Label mandatory = new TextLabel("*");

		final List<SubscriptionPeriodRow> periodRows = Lists.newArrayList();
		if( tier.isPurchase() )
		{
			final SubscriptionPeriodRow row = new SubscriptionPeriodRow();
			row.setLabel(LABEL_PURCHASE_PRICE);

			final TagState divState = new TagState("d");
			final HtmlValueState value = periodPrice.getValueState(context, KEY_PURCHASE);
			value.setId("pte_p0");
			final DivRenderer dr = new DivRenderer(divState, CombinedRenderer.combineMultipleResults(new LabelRenderer(
				currencyLabel), new TextFieldRenderer(value)));
			// CSS defined in edittier.css
			dr.addClasses("price");

			final TagState mandatoryTag = new TagState("mandatory");
			// CSS defined in edittier.css
			mandatoryTag.addClasses("pricemandatory");
			final SpanRenderer mandatorySpan = new SpanRenderer(mandatoryTag, mandatory);

			row.setValue(CombinedRenderer.combineMultipleResults(mandatorySpan, dr));
			row.setErrorKey("price.purchase.value");
			periodRows.add(row);
		}
		else
		{
			int index = 0;
			for( SubscriptionPeriod period : paymentService.enumerateSubscriptionPeriods() )
			{
				final String periodId = Long.toString(period.getId());
				final HtmlBooleanState periodBooleanState = periodEnabled.getBooleanState(context, periodId);
				periodBooleanState.setId("pte_e" + index);
				final HtmlValueState value = periodPrice.getValueState(context, periodId);
				value.setId("pte_p" + index);

				final TagState divState = new TagState("pte_d" + index);
				final DivRenderer dr = new DivRenderer(divState, CombinedRenderer.combineMultipleResults(
					new LabelRenderer(currencyLabel), new TextFieldRenderer(value)));
				// CSS defined in edittier.css
				dr.addClasses("price");

				final TagState mandatoryTag = new TagState("man_t" + index);
				mandatoryTag.addClasses("pricemandatory");
				final SpanRenderer sr = new SpanRenderer(mandatoryTag, mandatory);

				periodBooleanState.setClickHandler(new StatementHandler(PERIOD_ENABLED_FUNCTION, Jq
					.$(periodBooleanState),
					Jq.$(mandatoryTag), Jq.$(divState)));
				periodBooleanState.addReadyStatements(Js.call_s(PERIOD_ENABLED_FUNCTION, Jq.$(periodBooleanState),
					Jq.$(mandatoryTag),
					Jq.$(divState)));

				final SubscriptionPeriodRow row = new SubscriptionPeriodRow();
				row.setLabel(new BundleLabel(period.getName(), bundleCache));
				row.setEnabled(periodBooleanState);

				SectionRenderable combineMultipleResults = CombinedRenderer.combineMultipleResults(sr, dr);
				row.setValue(combineMultipleResults);
				row.setErrorKey("price.subscription." + period.getId() + ".value");
				periodRows.add(row);

				index++;
			}
		}
		model.setPeriods(periodRows);

		return view.createResult("edittier.ftl", this);
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	protected AbstractEntityService<PricingTierEditingBean, PricingTier> getEntityService()
	{
		return tierService;
	}

	@Override
	protected PricingTier createNewEntity(SectionInfo info)
	{
		final PricingTier tier = new PricingTier();
		final TierEditorModel model = getModel(info);
		tier.setPurchase(model.isPurchase());
		return tier;
	}

	@Override
	protected void loadFromSession(SectionInfo info, EntityEditingSession<PricingTierEditingBean, PricingTier> session)
	{
		final PricingTierEditingBean bean = session.getBean();
		getModel(info).setPurchase(bean.isPurchase());

		final Map<String, String> priceMap = new HashMap<String, String>();
		final Set<String> periodEnabledSet = Sets.newHashSet();

		if( bean.isPurchase() )
		{
			PriceBean price = bean.getPurchasePrice();
			priceMap.put(KEY_PURCHASE, price == null ? "" : price.getValue());
		}
		else
		{
			for( PriceBean price : bean.getSubscriptionPrices() )
			{
				final String periodString = Long.toString(price.getSubscriptionPeriodId());
				if( price.isEnabled() )
				{
					periodEnabledSet.add(periodString);
				}
				priceMap.put(periodString, price.getValue());
			}
		}

		periodEnabled.setCheckedSet(info, periodEnabledSet);
		periodPrice.setValuesMap(info, priceMap);
		enabled.setChecked(info, bean.isEnabled());
	}

	@Override
	protected void saveToSession(SectionInfo info, EntityEditingSession<PricingTierEditingBean, PricingTier> session,
		boolean validate)
	{
		final String currency = paymentWebService.getSettings(info).getCurrency();
		final Map<String, String> valuesMap = periodPrice.getValuesMap(info);
		final PricingTierEditingBean bean = session.getBean();

		if( bean.isPurchase() )
		{
			PriceBean pb = bean.getPurchasePrice();
			if( pb == null )
			{
				pb = new PriceBean();
				bean.setPurchasePrice(pb);
			}
			pb.setEnabled(true);
			pb.setCurrency(currency);
			pb.setValue(valuesMap.get(KEY_PURCHASE));
		}
		else
		{
			// to preserve existing price IDs
			final Map<Long, PriceBean> priceMap = Maps.newHashMap();
			final List<PriceBean> pricesList = bean.getSubscriptionPrices();
			for( PriceBean price : pricesList )
			{
				priceMap.put(price.getSubscriptionPeriodId(), price);
			}
			pricesList.clear();

			final Set<String> enabledPrices = periodEnabled.getCheckedSet(info);

			for( Entry<String, String> entry : valuesMap.entrySet() )
			{
				final String periodId = entry.getKey();
				PriceBean pb = priceMap.get(Long.valueOf(periodId));
				if( pb == null )
				{
					pb = new PriceBean();
					pb.setSubscriptionPeriodId(Long.valueOf(periodId));
				}
				pb.setEnabled(enabledPrices.contains(periodId));
				pb.setCurrency(currency);
				pb.setValue(entry.getValue());
				pricesList.add(pb);
			}
		}
		bean.setEnabled(enabled.isChecked(info));
	}

	public void setPurchase(SectionInfo info, boolean purchase)
	{
		getModel(info).setPurchase(purchase);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new TierEditorModel();
	}

	public Checkbox getEnabled()
	{
		return enabled;
	}

	public class TierEditorModel
		extends
			AbstractEntityEditor<PricingTierEditingBean, PricingTier, TierEditorModel>.AbstractEntityEditorModel
	{
		private boolean purchase;
		private List<SubscriptionPeriodRow> periods;

		public boolean isPurchase()
		{
			return purchase;
		}

		public void setPurchase(boolean purchase)
		{
			this.purchase = purchase;
		}

		public List<SubscriptionPeriodRow> getPeriods()
		{
			return periods;
		}

		public void setPeriods(List<SubscriptionPeriodRow> periods)
		{
			this.periods = periods;
		}
	}

	public static class SubscriptionPeriodRow
	{
		private Label label;
		private HtmlBooleanState enabled;
		private SectionRenderable value;
		private String errorKey;

		public Label getLabel()
		{
			return label;
		}

		public void setLabel(Label label)
		{
			this.label = label;
		}

		public HtmlBooleanState getEnabled()
		{
			return enabled;
		}

		public void setEnabled(HtmlBooleanState enabled)
		{
			this.enabled = enabled;
		}

		public SectionRenderable getValue()
		{
			return value;
		}

		public void setValue(SectionRenderable value)
		{
			this.value = value;
		}

		public String getErrorKey()
		{
			return errorKey;
		}

		public void setErrorKey(String errorKey)
		{
			this.errorKey = errorKey;
		}
	}
}
