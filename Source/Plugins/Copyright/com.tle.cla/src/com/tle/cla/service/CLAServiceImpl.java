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

package com.tle.cla.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.cla.CLAHolding;
import com.tle.beans.cla.CLAPortion;
import com.tle.beans.cla.CLASection;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.Item;
import com.tle.cla.CLAConstants;
import com.tle.cla.dao.CLADao;
import com.tle.common.i18n.LangUtils;
import com.tle.core.activation.ActivateRequestDao;
import com.tle.core.activation.validation.ActivationPeriodHelper;
import com.tle.core.copyright.exception.CopyrightViolationException;
import com.tle.core.copyright.service.AbstractCopyrightService;
import com.tle.core.guice.Bind;

@Bind(CLAService.class)
@Singleton
public class CLAServiceImpl extends AbstractCopyrightService<CLAHolding, CLAPortion, CLASection> implements CLAService
{
	private final CLADao claDao;

	@Inject
	private ActivateRequestDao requestDao;

	@Inject
	public CLAServiceImpl(CLADao claDao)
	{
		super(claDao);
		this.claDao = claDao;
	}

	@Override
	public String getActivationType()
	{
		return CLAConstants.ACTIVATION_TYPE;
	}

	@Override
	protected String getAgreementFileAttribute()
	{
		return CLAConstants.AGREEMENTFILE;
	}

	@Override
	protected String getEnabledAttribute()
	{
		return CLAConstants.ENABLED;
	}

	@Override
	protected String getHasAgreementAttribute()
	{
		return CLAConstants.HASAGREEMENT;
	}

	@Override
	protected String getInactiveErrorAttribute()
	{
		return CLAConstants.INACTIVEERROR;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void validateHolding(CLAHolding holding)
	{
		List<Item> items = claDao.getAllItemsForHolding(holding);
		List<ActivateRequest> requests = requestDao.getAllRequestsForItems(getActivationType(), items);
		ensureStates(requests);

		CLAValidation status = new CLAValidation(holding);

		Map<String, List<ActivateRequest>> requestsMap = new HashMap<String, List<ActivateRequest>>();
		for( ActivateRequest request : requests )
		{
			String courseId = request.getCourse().getUuid();
			List<ActivateRequest> activityList = requestsMap.get(courseId);
			if( activityList == null )
			{
				activityList = new ArrayList<ActivateRequest>();
				requestsMap.put(courseId, activityList);
			}
			activityList.add(request);
		}

		boolean valid = true;
		for( List<ActivateRequest> perCourse : requestsMap.values() )
		{
			if( !validateRequests(status, perCourse) )
			{
				valid = false;
				break;
			}
		}

		if( !valid )
		{
			LanguageBundle calError = LangUtils.getBundleFromXmlString(holding.getItem().getItemDefinition()
				.getAttributes().get(CLAConstants.ACTIVATIONERROR));
			throw new CopyrightViolationException(calError);
		}
	}

	private boolean validateRequests(CLAValidation status, List<ActivateRequest> requests)
	{
		ActivationPeriodHelper helper = new ActivationPeriodHelper(requests);
		for( Date time : helper.calculatePoints() )
		{
			status.reset();
			List<ActivateRequest> intersects = helper.calculateIntersections(time);
			for( ActivateRequest request : intersects )
			{
				if( request.getStatus() == ActivateRequest.TYPE_ACTIVE )
				{
					status.addActive(request);
				}
			}

			if( !status.isValid() )
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public String citate(CLAHolding holding, CLAPortion portion)
	{
		return null;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void validateItem(Item item, boolean ignoreOverrides, boolean skipPercentage)
	{
		validateHolding(getHoldingForItem(item));
	}
}
