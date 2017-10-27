/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.quota.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.tle.beans.Institution;
import com.tle.common.FileSizeUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.email.EmailResult;
import com.tle.core.email.EmailService;
import com.tle.core.guice.Bind;
import com.tle.core.quota.service.QuotaService;
import com.tle.core.scheduler.ScheduledTask;

@Bind
@Singleton
public class CheckInstitutionStorageQuotas implements ScheduledTask
{
	private static final Logger LOGGER = Logger.getLogger(CheckInstitutionStorageQuotas.class);

	@Inject
	private QuotaService quotaService;
	@Inject
	private EmailService emailService;

	@Override
	public void execute()
	{
		Collection<Institution> availableInsts = quotaService.getInstitutionsWithFilestoreLimits();
		List<Institution> instsOverLimit = new ArrayList<Institution>();

		for( Institution inst : availableInsts )
		{
			quotaService.refreshCache(inst);
			if( quotaService.isInstitutionOverLimit(inst) )
			{
				instsOverLimit.add(inst);
			}
		}

		if( !instsOverLimit.isEmpty() )
		{
			String subject = CurrentLocale
				.get("com.tle.core.entity.services.institutions.warninglimitexceeded.subject");
			String message = buildEmailMessage(instsOverLimit);
			Future<EmailResult<String>> result = emailService.sendSystemEmail(subject, message);
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

	private String buildEmailMessage(List<Institution> insts)
	{
		StringBuilder emailMessage = new StringBuilder();
		if( insts.size() > 1 )
		{
			emailMessage
				.append(CurrentLocale.get("com.tle.core.entity.services.institutions.warninglimitexceeded.text"));

		}
		else
		{
			emailMessage
				.append(CurrentLocale.get("com.tle.core.entity.services.institutions.warninglimitexceeded.text.1"));
		}
		emailMessage.append("\n\n");
		for( Institution inst : insts )
		{
			emailMessage.append(inst.getName());
			emailMessage.append("\n");
			emailMessage.append(
				CurrentLocale.get("com.tle.core.entity.services.institutions.warninglimitexceeded.limit") + " ");
			emailMessage.append(inst.getQuota() + " GB ");
			emailMessage.append(
				CurrentLocale.get("com.tle.core.entity.services.institutions.warninglimitexceeded.currentusage") + " ");
			emailMessage.append(FileSizeUtils.humanReadableGigabyte(quotaService.getInstitutionalConsumption(inst)));
			emailMessage.append("\n\n");
		}

		return emailMessage.toString();

	}
}
