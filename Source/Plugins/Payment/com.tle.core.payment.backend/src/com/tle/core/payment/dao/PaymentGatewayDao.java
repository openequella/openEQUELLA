package com.tle.core.payment.dao;

import java.util.List;

import com.tle.common.payment.entity.PaymentGateway;
import com.tle.common.payment.entity.Region;
import com.tle.core.dao.AbstractEntityDao;

public interface PaymentGatewayDao extends AbstractEntityDao<PaymentGateway>
{
	List<PaymentGateway> enumerateAllByRegion(Region region);

	List<PaymentGateway> enumerateEnabledByType(String gatewayType);
}
