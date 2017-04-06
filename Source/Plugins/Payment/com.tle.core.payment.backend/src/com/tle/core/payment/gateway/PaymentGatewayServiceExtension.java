package com.tle.core.payment.gateway;

import com.tle.common.payment.entity.PaymentGateway;
import com.tle.core.payment.service.session.PaymentGatewayEditingBean;

public interface PaymentGatewayServiceExtension
{
	void deleteExtra(PaymentGateway gateway);

	void edit(PaymentGateway entity, PaymentGatewayEditingBean bean);

	void add(PaymentGateway gateway);

	void loadExtra(PaymentGateway gateway);
}
