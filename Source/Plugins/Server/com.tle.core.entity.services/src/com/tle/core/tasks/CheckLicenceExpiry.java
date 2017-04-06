package com.tle.core.tasks;

import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.dytech.edge.common.valuebean.License;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.email.EmailResult;
import com.tle.core.email.EmailService;
import com.tle.core.guice.Bind;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.system.LicenseService;

@Bind
@Singleton
public class CheckLicenceExpiry implements ScheduledTask
{
	private static final Logger LOGGER = Logger.getLogger(CheckLicenceExpiry.class);

	@Inject
	private LicenseService licenseService;
	@Inject
	private EmailService emailService;

	@SuppressWarnings("nls")
	@Override
	public void execute()
	{
		License license = licenseService.getLicense();
		Date expiryDate = license.getExpiry();
		// 30 days
		long timePeriod = TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS);
		boolean isWarned = (expiryDate.getTime() - System.currentTimeMillis()) < timePeriod;
		if( isWarned )
		{
			String subject = CurrentLocale.get("com.tle.core.entity.services.license.expirynotificationemail.subject");
			String text = CurrentLocale.get("com.tle.core.entity.services.license.expirynotificationemail.text",
				license.getExpiry());

			Future<EmailResult<String>> result = emailService.sendSystemEmail(subject, text);

			try
			{
				result.get();
			}
			catch( Exception e )
			{
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
}
