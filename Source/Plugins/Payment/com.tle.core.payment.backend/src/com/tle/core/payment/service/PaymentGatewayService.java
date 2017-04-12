package com.tle.core.payment.service;

import java.util.List;

import com.tle.common.payment.entity.PaymentGateway;
import com.tle.core.payment.gateway.GatewayTypeDescriptor;
import com.tle.core.payment.gateway.PaymentGatewayCheckoutInfo;
import com.tle.core.payment.service.session.PaymentGatewayEditingBean;
import com.tle.core.services.entity.AbstractEntityService;

public interface PaymentGatewayService extends AbstractEntityService<PaymentGatewayEditingBean, PaymentGateway>
{
	String ENTITY_TYPE = "PAYMENT_GATEWAY"; //$NON-NLS-1$

	Iterable<GatewayTypeDescriptor> listAllAvailableTypes();

	PaymentGateway getForEdit(String uuid);

	List<String> getImplementationTypes();

	String testGateway(PaymentGatewayEditingBean editedEntity);

	boolean canEnable(String gatewayType, long id);

	/**
	 * Gets additional information such as 'pay now' icon URL and checkout URL
	 * 
	 * @param gateway
	 * @return
	 */
	PaymentGatewayCheckoutInfo getCheckoutInfo(PaymentGateway gateway);

	PaymentGateway getEnabledGateway(String type);
}
