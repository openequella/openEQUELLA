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

package com.tle.web.activation.soap;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.cal.CALConstants;
import com.tle.cal.service.CALService;
import com.tle.cla.CLAConstants;
import com.tle.cla.service.CLAService;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.guice.Bind;

/**
 * @author aholland
 */
@Bind(SoapActivationService.class)
@Singleton
public class SoapActivationServiceImpl implements SoapActivationService
{
	@Inject
	private CourseInfoService courseInfoService;
	@Inject
	private ActivationService activationService;
	@Inject
	private CALService calService;
	@Inject
	private CLAService claService;

	/**
	 * Activates the copyright on said attachments
	 * 
	 * @param uuid Item uuid
	 * @param version Item version
	 * @param course Course code
	 * @param attachments List of attachment uuids
	 */
	@Override
	public void activateItemAttachments(String uuid, int version, String courseCode, String[] attachments)
	{
		String activationType = CLAConstants.ACTIVATION_TYPE;
		ItemId itemId = new ItemId(uuid, version);
		Item item = claService.getCopyrightedItem(itemId);
		if( item == null )
		{
			item = calService.getCopyrightedItem(itemId);
			activationType = CALConstants.ACTIVATION_TYPE;
		}
		CourseInfo course = courseInfoService.getByCode(courseCode);
		if( course == null )
		{
			throw new NotFoundException(CurrentLocale.get("com.tle.cal.web.soapservice.coursenotfound", courseCode)); //$NON-NLS-1$
		}

		List<ActivateRequest> requests = new ArrayList<ActivateRequest>(attachments.length);
		for( int i = 0; i < attachments.length; i++ )
		{
			ActivateRequest request = new ActivateRequest();
			request.setItem(item);
			request.setCourse(course);
			request.setAttachment(attachments[i]);
			request.setFrom(course.getFrom());
			request.setUntil(course.getUntil());
			request.setCitation(course.getCitation());
			requests.add(request);
		}
		activationService.activate(activationType, item, requests, false);
	}
}
