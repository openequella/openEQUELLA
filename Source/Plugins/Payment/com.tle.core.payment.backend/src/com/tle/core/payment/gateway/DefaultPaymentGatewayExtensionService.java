package com.tle.core.payment.gateway;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageBundle.DeleteHandler;
import com.tle.common.i18n.beans.LanguageBundleBean;
import com.tle.common.payment.entity.PaymentGateway;
import com.tle.core.guice.Bind;
import com.tle.core.payment.dao.PaymentGatewayDao;
import com.tle.core.payment.service.session.PaymentGatewayEditingBean;

@Bind
@Singleton
public class DefaultPaymentGatewayExtensionService implements PaymentGatewayServiceExtension
{
	@Inject
	private PaymentGatewayDao paymentGatewayDao;

	@Override
	public void deleteExtra(PaymentGateway gateway)
	{
		// nothing
	}

	@Override
	public void edit(PaymentGateway to, PaymentGatewayEditingBean from)
	{
		to.setOwner(from.getOwner());
		to.setName(editBundle(to.getName(), from.getName()));
		to.setDescription(editBundle(to.getDescription(), from.getDescription()));
		to.setAttributes(from.getAttributes());
		to.setDisabled(!from.isEnabled());
	}

	private LanguageBundle editBundle(LanguageBundle oldBundle, LanguageBundleBean newBundle)
	{
		return LanguageBundle.edit(oldBundle, newBundle, new DeleteHandler()
		{
			@SuppressWarnings("synthetic-access")
			@Override
			public void deleteBundleObject(Object obj)
			{
				paymentGatewayDao.deleteAny(obj);
			}
		});
	}

	@Override
	public void add(PaymentGateway gateway)
	{
		// nothing
	}

	@Override
	public void loadExtra(PaymentGateway gateway)
	{
		// nothing
	}
}
