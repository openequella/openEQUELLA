package com.tle.web.payment.paypal;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.payment.entity.PaymentGateway;
import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentGatewayConstants;
import com.tle.core.payment.gateway.PaymentGatewayCheckoutInfo;
import com.tle.core.payment.gateway.PaymentGatewayCheckoutInfoExtension;
import com.tle.core.services.UrlService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class PaypalCheckoutInfoExtension implements PaymentGatewayCheckoutInfoExtension
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(PaypalCheckoutInfoExtension.class);
	@Inject
	private UrlService urlService;

	@Override
	public PaymentGatewayCheckoutInfo getCheckoutInfo(PaymentGateway gateway)
	{
		final PaymentGatewayCheckoutInfo info = new PaymentGatewayCheckoutInfo();
		final String button;
		if( gateway.getAttribute(PaymentGatewayConstants.SANDBOX_KEY, false) )
		{
			button = resources.url("images/sandboxcheckoutbutton.gif");
		}
		else
		{
			button = "https://www.paypal.com/en_US/i/btn/btn_xpressCheckout.gif";
		}
		// info.setButtonImageUrl(urlService.institutionalise(button));
		info.setButtonImageUrl(urlService.institutionalise(button));
		info.setCheckoutUrl(urlService.institutionalise("paypal/checkout.do"));
		return info;
	}
}
