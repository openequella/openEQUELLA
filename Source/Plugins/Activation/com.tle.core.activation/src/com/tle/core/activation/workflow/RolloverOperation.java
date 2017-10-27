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

package com.tle.core.activation.workflow;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.core.activation.ActivateRequestDao;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.common.usermanagement.user.CurrentUser;

/**
 * @author aholland
 */
@SecureOnCall(priv = "COPYRIGHT_ITEM")
public class RolloverOperation extends AbstractBulkableActivationOperation
{
	private long courseId;
	private Date from;
	private Date until;
	private boolean cancel;
	private boolean sameCourse;
	private boolean rolloverDates;

	@Inject
	private CourseInfoService courseService;

	@Inject
	private ActivationService activationService;

	@AssistedInject
	public RolloverOperation(@Assisted long courseId, @Assisted("from") Date from, @Assisted("until") Date until)
	{
		super();
		this.courseId = courseId;
		this.from = from;
		this.until = until;
	}

	public void setUseSameCourse(boolean sameCourse)
	{
		this.sameCourse = sameCourse;
	}

	public void setRolloverDates(boolean rolloverDates)
	{
		this.rolloverDates = rolloverDates;
	}

	public void setCancel(boolean cancel)
	{
		this.cancel = cancel;
	}

	@Override
	protected boolean doOperation(ActivateRequest request, ActivateRequestDao dao)
	{
		ActivateRequest rolledOverRequest;
		try
		{
			rolledOverRequest = (ActivateRequest) request.clone();
			rolledOverRequest.setId(0);
			rolledOverRequest.setUser(CurrentUser.getUserID());
			rolledOverRequest.setUuid(UUID.randomUUID().toString());
			if( !sameCourse )
			{
				CourseInfo course = courseService.get(courseId);
				rolledOverRequest.setCourse(course);
			}
			if( rolloverDates )
			{
				incrementYears(rolledOverRequest);
			}
			else
			{
				rolledOverRequest.setFrom(from);
				rolledOverRequest.setUntil(until);
			}
			rolledOverRequest.setTime(params.getDateNow());
			rolledOverRequest.setStatus(ActivateRequest.TYPE_PENDING);
			dao.save(rolledOverRequest);
		}
		catch( CloneNotSupportedException e )
		{
			throw new RuntimeException(e);
		}
		if( cancel && request.getUntil().after(from) )
		{
			// If new start date < now then deactivate old
			// otherwise set end date of old activation to start date of new
			// activation
			if( from.before(new Date()) )
			{
				activationService.deactivateByUuid(request.getUuid());
			}
			else
			{
				request.setUntil(from);
				dao.update(request);
			}
		}
		activationService.validateItem(request.getType(), request.getItem(), true, false);
		return true;
	}

	private void incrementYears(ActivateRequest rolledOverRequest)
	{
		Calendar cal = Calendar.getInstance(CurrentTimeZone.get(), CurrentLocale.getLocale());
		cal.setTime(rolledOverRequest.getFrom());
		cal.roll(Calendar.YEAR, true);
		rolledOverRequest.setFrom(cal.getTime());
		cal.setTime(rolledOverRequest.getUntil());
		cal.roll(Calendar.YEAR, true);
		rolledOverRequest.setUntil(cal.getTime());

	}
}
