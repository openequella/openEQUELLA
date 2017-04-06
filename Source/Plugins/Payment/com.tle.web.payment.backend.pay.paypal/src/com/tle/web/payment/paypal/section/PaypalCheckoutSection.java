package com.tle.web.payment.paypal.section;

import java.util.Currency;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.payment.entity.PaymentGateway;
import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.SaleItem;
import com.tle.core.payment.PaymentGatewayConstants;
import com.tle.core.payment.paypal.PaypalConstants;
import com.tle.core.payment.paypal.PaypalGatewayService;
import com.tle.core.payment.service.PaymentGatewayService;
import com.tle.core.payment.service.SaleService;
import com.tle.core.services.UrlService;
import com.tle.core.services.item.ItemService;
import com.tle.web.payment.paypal.section.PaypalCheckoutSection.PaypalCheckoutModel;
import com.tle.web.payment.section.pay.AbstractCheckoutSection;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.SubmitEventFunction;
import com.tle.web.sections.header.SimpleFormAction;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.HiddenInput;
import com.tle.web.sections.result.util.MoneyLabel;
import com.tle.web.template.Decorations;

public class PaypalCheckoutSection extends AbstractCheckoutSection<PaypalCheckoutModel>
{
	@PlugKey("checkout.error.unsupportedcurrency")
	private static String KEY_ERROR_UNSUPPORTED_CURRENCY;

	@Inject
	private ItemService itemService;
	@Inject
	private UrlService urlService;
	@Inject
	private PaypalGatewayService paypalService;
	@Inject
	private PaymentGatewayService gatewayService;
	@Inject
	private SaleService saleService;

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		PaypalCheckoutModel model = getModel(context);

		final PaymentGateway gateway = gatewayService.getByUuid(model.getGatewayUuid());
		Sale sale = saleService.getSale(null, model.getCheckoutUuid());

		Currency currency = sale.getCurrency();
		// blow up if unsupported currency
		if( !paypalService.isSupportedCurrency(currency) )
		{
			throw new RuntimeException(CurrentLocale.get(KEY_ERROR_UNSUPPORTED_CURRENCY, currency.getCurrencyCode()));
		}

		HiddenInput hiddenInput = new HiddenInput();
		hiddenInput.addField(null, "cmd", "_cart");
		hiddenInput.addField(null, "upload", "1");
		hiddenInput.addField(null, "business", gateway.getAttribute(PaypalConstants.USERNAME_KEY));
		hiddenInput.addField(null, "no_shipping", "1");
		hiddenInput.addField(null, "return", model.getReturnUrl());
		hiddenInput.addField(null, "invoice", sale.getUuid());
		hiddenInput.addField(null, "notify_url", urlService.institutionalise("paypal-notification/"));
		hiddenInput.addField(null, "currency_code", currency.getCurrencyCode());

		int index = 1;
		for( SaleItem saleItem : sale.getSales() )
		{
			String itemUuid = saleItem.getItemUuid();
			int itemVersion = itemService.getLiveItemVersion(itemUuid);
			ItemId itemId = new ItemId(itemUuid, itemVersion);
			Item item = itemService.getUnsecure(itemId);

			hiddenInput.addField(null, "item_name_" + index, CurrentLocale.get(item.getName()));
			hiddenInput.addField(null, "item_number_" + index, CurrentLocale.get(item.getName()));
			hiddenInput.addField(null, "amount_" + index, new MoneyLabel(saleItem.getUnitPrice(), currency, false,
				false, false).getText());
			hiddenInput.addField(null, "tax_" + index, new MoneyLabel(saleItem.getUnitTax(), currency, false, false,
				false).getText());
			hiddenInput.addField(null, "quantity_" + index, Integer.toString(saleItem.getQuantity()));
			index++;
		}

		boolean sandbox = gateway.getAttribute(PaymentGatewayConstants.SANDBOX_KEY, false);

		String action = sandbox ? "https://www.sandbox.paypal.com/cgi-bin/webscr"
			: "https://www.paypal.com/cgi-bin/webscr";
		Decorations.getDecorations(context).clearAllDecorations();
		context.getForm().setAction(new SimpleFormAction(action));
		context.getBody().addReadyStatements(new SubmitEventFunction());
		return CombinedRenderer.combineMultipleResults(hiddenInput);
	}

	@Override
	public Class<PaypalCheckoutModel> getModelClass()
	{
		return PaypalCheckoutModel.class;
	}

	public static class PaypalCheckoutModel extends AbstractCheckoutSection.AbstractCheckoutModel
	{
		// Nada
	}
}
