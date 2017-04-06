package com.tle.core.payment.gateway;

import com.tle.core.payment.service.session.PaymentGatewayEditingBean;

public interface PaymentGatewayImplementation
{
	String testCredentials(PaymentGatewayEditingBean gateway);
}
