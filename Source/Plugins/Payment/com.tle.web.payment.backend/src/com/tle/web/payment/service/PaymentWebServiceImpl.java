package com.tle.web.payment.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentSettings;
import com.tle.core.services.config.ConfigurationService;
import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
@Bind(PaymentWebService.class)
@Singleton
public class PaymentWebServiceImpl implements PaymentWebService
{

	@Inject
	private ConfigurationService configService;

	@Override
	public PaymentSettings getSettings(SectionInfo info)
	{
		PaymentSettings settings = info.getAttribute(PaymentSettings.class);

		if( settings == null )
		{
			settings = configService.getProperties(new PaymentSettings());
			info.setAttribute(PaymentSettings.class, settings);
		}
		return settings;
	}

	@Override
	public void saveSettings(SectionInfo info, PaymentSettings settings)
	{
		configService.setProperties(settings);
		info.setAttribute(PaymentSettings.class, settings);
	}
}
