package com.tle.core.payment.gateway;

import com.tle.common.payment.entity.PaymentGateway;

/**
 * @author Aaron
 */
public interface PaymentGatewayCheckoutInfoExtension
{
	PaymentGatewayCheckoutInfo getCheckoutInfo(PaymentGateway gateway);
}
