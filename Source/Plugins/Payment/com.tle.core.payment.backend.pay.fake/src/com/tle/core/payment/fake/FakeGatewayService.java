package com.tle.core.payment.fake;

import com.tle.beans.Institution;
import com.tle.common.payment.entity.PaymentGateway;
import com.tle.core.services.impl.Task;

public interface FakeGatewayService
{
	void pay(PaymentGateway gateway, String saleUuid);

	Task createFakeTask(Institution institution, String saleUuid);
}
