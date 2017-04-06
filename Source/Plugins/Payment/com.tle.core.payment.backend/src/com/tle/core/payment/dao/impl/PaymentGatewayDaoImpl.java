package com.tle.core.payment.dao.impl;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Query;

import com.tle.common.payment.entity.PaymentGateway;
import com.tle.common.payment.entity.Region;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.payment.dao.PaymentGatewayDao;

@Bind(PaymentGatewayDao.class)
@Singleton
@SuppressWarnings("nls")
public class PaymentGatewayDaoImpl extends AbstractEntityDaoImpl<PaymentGateway> implements PaymentGatewayDao
{
	public PaymentGatewayDaoImpl()
	{
		super(PaymentGateway.class);
	}

	@Override
	public List<PaymentGateway> enumerateAllByRegion(final Region region)
	{
		return enumerateAll(new BaseCallback()
		{
			@Override
			public String getAdditionalWhere()
			{
				return ":region IN elements(regions)";
			}

			@Override
			public void processQuery(Query query)
			{
				query.setParameter("region", region);
			}
		});
	}

	@Override
	public List<PaymentGateway> enumerateEnabledByType(final String gatewayType)
	{
		return enumerateAll(new TypeCallback(gatewayType, true));
	}

	private static class TypeCallback extends EnabledCallback
	{
		private final String type;

		public TypeCallback(String type, Boolean enabled)
		{
			super(enabled);
			this.type = type;
		}

		@Override
		public String getAdditionalWhere()
		{
			final String where = super.getAdditionalWhere();
			if( type != null )
			{
				return appendWhere(where, "gatewayType = :gatewayType ");
			}
			return where;
		}

		@Override
		public void processQuery(Query query)
		{
			super.processQuery(query);
			if( type != null )
			{
				query.setParameter("gatewayType", type);
			}
		}
	}
}
