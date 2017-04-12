package com.tle.core.payment.service.session;

import com.tle.common.EntityPack;
import com.tle.common.payment.entity.PaymentGateway;
import com.tle.core.services.entity.impl.EntityEditingSessionImpl;

public class PaymentGatewayEditingSession extends EntityEditingSessionImpl<PaymentGatewayEditingBean, PaymentGateway>
{
	private static final long serialVersionUID = 1L;

	public PaymentGatewayEditingSession(String sessionId, EntityPack<PaymentGateway> pack,
		PaymentGatewayEditingBean bean)
	{
		super(sessionId, pack, bean);
	}
}
