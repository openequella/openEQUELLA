package com.tle.web.payment.fake;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.payment.entity.PaymentGateway;
import com.tle.core.guice.Bind;
import com.tle.core.payment.gateway.PaymentGatewayCheckoutInfo;
import com.tle.core.payment.gateway.PaymentGatewayCheckoutInfoExtension;
import com.tle.core.services.UrlService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class FakeCheckoutInfoExtension implements PaymentGatewayCheckoutInfoExtension
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(FakeCheckoutInfoExtension.class);

	@Inject
	private UrlService urlService;

	@Override
	public PaymentGatewayCheckoutInfo getCheckoutInfo(PaymentGateway gateway)
	{
		final PaymentGatewayCheckoutInfo info = new PaymentGatewayCheckoutInfo();
		info.setButtonImageUrl(urlService.institutionalise(resources.url("images/democheckoutbutton.png")));
		info.setCheckoutUrl(urlService.institutionalise("fake/checkout.do"));
		return info;
	}
}
