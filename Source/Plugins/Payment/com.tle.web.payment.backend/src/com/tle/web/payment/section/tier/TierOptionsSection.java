package com.tle.web.payment.section.tier;

import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.core.payment.PaymentSettings;
import com.tle.core.payment.service.PaymentService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.payment.service.PaymentWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.model.SimpleOption;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class TierOptionsSection extends AbstractPrototypeSection<TierOptionsSection.TierOptionsModel>
	implements
		HtmlRenderer
{
	private static final String AJAX_ID = "topt";

	@PlugKey("tier.showlist.label.warning")
	private static Label LABEL_CURRENCY_CHANGE_WARNING;
	@PlugKey("tier.showlist.label.currency.select")
	private static Label LABEL_CURRENCY_SELECT;

	@Component(name = "c", stateful = false)
	private SingleSelectionList<Currency> currency;
	@PlugKey("tier.showlist.pricingmodels.option.free")
	@Component(name = "f", stateful = false)
	private Checkbox free;
	@PlugKey("tier.showlist.pricingmodels.option.purchase")
	@Component(name = "p", stateful = false)
	private Checkbox purchase;
	@PlugKey("tier.showlist.pricingmodels.option.subscription")
	@Component(name = "s", stateful = false)
	private Checkbox subscription;

	@Inject
	private PaymentService paymentService;
	@Inject
	private PaymentWebService paymentWebService;

	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;
	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final PaymentSettings settings = paymentWebService.getSettings(context);

		currency.setSelectedStringValue(context, settings.getCurrency());
		free.setChecked(context, settings.isFreeEnabled());
		purchase.setChecked(context, settings.isPurchaseEnabled());
		subscription.setChecked(context, settings.isSubscriptionEnabled());

		final TierOptionsModel model = getModel(context);
		model.setRenderable(renderChildren(context, new ResultListCollector(true)).getFirstResult());

		return view.createResult("tieroptions.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		final List<Currency> currencies = Lists.newArrayList(paymentService.getCurrencies());
		Collections.sort(currencies, new Comparator<Currency>()
		{
			@Override
			public int compare(Currency c1, Currency c2)
			{
				return c1.getCurrencyCode().compareTo(c2.getCurrencyCode());
			}
		});
		currency.setListModel(new SimpleHtmlListModel<Currency>(currencies)
		{
			@Override
			protected Option<Currency> convertToOption(Currency obj)
			{
				return new SimpleOption<Currency>(obj.getCurrencyCode(), obj.getCurrencyCode(), obj);
			}
		});

		final JSHandler handler = new StatementHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("refresh"), ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING), AJAX_ID,
			ShowPurchaseTiersSection.AJAX_ID, ShowSubscriptionTiersSection.AJAX_ID));
		free.setEventHandler("change", handler);
		purchase.setEventHandler("change", handler);
		subscription.setEventHandler("change", handler);

		// Includes the tiers in the AJAX update as the price formats will
		// change
		final JSHandler currencyChangeHandler = new StatementHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("currencyChange"), ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING), AJAX_ID,
			ShowPurchaseTiersSection.AJAX_ID, ShowSubscriptionTiersSection.AJAX_ID));
		currencyChangeHandler.addValidator(new Confirm(LABEL_CURRENCY_CHANGE_WARNING));
		currency.setEventHandler("change", currencyChangeHandler);
	}

	@EventHandlerMethod
	public void currencyChange(SectionInfo info)
	{
		final PaymentSettings settings = paymentWebService.getSettings(info);
		final Currency newCurrency = currency.getSelectedValue(info);
		if( newCurrency != null )
		{
			settings.setCurrency(newCurrency.getCurrencyCode());
			paymentWebService.saveSettings(info, settings);

			// probably shouldn't rely on UI to do this
			paymentService.changeCurrency(null, null, newCurrency);
		}
		else
		{
			getModel(info).getErrors().put("currency", LABEL_CURRENCY_SELECT);
		}
	}

	@EventHandlerMethod
	public void refresh(SectionInfo info)
	{
		final PaymentSettings settings = paymentWebService.getSettings(info);
		settings.setCurrency(currency.getSelectedValue(info).getCurrencyCode());
		settings.setFreeEnabled(free.isChecked(info));
		settings.setPurchaseEnabled(purchase.isChecked(info));
		settings.setSubscriptionEnabled(subscription.isChecked(info));
		paymentWebService.saveSettings(info, settings);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new TierOptionsModel();
	}

	public SingleSelectionList<Currency> getCurrency()
	{
		return currency;
	}

	public Checkbox getFree()
	{
		return free;
	}

	public Checkbox getPurchase()
	{
		return purchase;
	}

	public Checkbox getSubscription()
	{
		return subscription;
	}

	public static class TierOptionsModel
	{
		private SectionRenderable renderable;
		private final Map<String, Label> errors = Maps.newHashMap();

		public SectionRenderable getRenderable()
		{
			return renderable;
		}

		public void setRenderable(SectionRenderable renderable)
		{
			this.renderable = renderable;
		}

		public Map<String, Label> getErrors()
		{
			return errors;
		}
	}
}
