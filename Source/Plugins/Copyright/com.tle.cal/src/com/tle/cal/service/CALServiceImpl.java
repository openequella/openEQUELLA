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

package com.tle.cal.service;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.Item;
import com.tle.cal.CALConstants;
import com.tle.cal.CitationGenerator;
import com.tle.cal.dao.CALDao;
import com.tle.common.i18n.LangUtils;
import com.tle.core.activation.ActivateRequestDao;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.copyright.exception.CopyrightViolationException;
import com.tle.core.copyright.service.AbstractCopyrightService;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;

@Bind(CALService.class)
@Singleton
public class CALServiceImpl extends AbstractCopyrightService<CALHolding, CALPortion, CALSection> implements CALService
{
	private final CALDao calDao;

	@Inject
	private CitationGenerator citationGen;
	@Inject
	private ItemService itemService;
	@Inject
	private ActivateRequestDao requestDao;
	@Inject
	private ActivationService activationService;

	@Inject
	public CALServiceImpl(CALDao calDao)
	{
		super(calDao);
		this.calDao = calDao;
	}

	@Override
	public String getActivationType()
	{
		return CALConstants.ACTIVATION_TYPE;
	}

	@Override
	protected String getEnabledAttribute()
	{
		return CALConstants.ENABLED;
	}

	@Override
	protected String getAgreementFileAttribute()
	{
		return CALConstants.AGREEMENTFILE;
	}

	@Override
	protected String getHasAgreementAttribute()
	{
		return CALConstants.HASAGREEMENT;
	}

	@Override
	protected String getInactiveErrorAttribute()
	{
		return CALConstants.INACTIVEERROR;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void validateHolding(CALHolding holding, boolean ignoreOverrides, boolean skipPercentage)
	{
		List<Item> items = calDao.getAllItemsForHolding(holding);
		List<ActivateRequest> requests = requestDao.getAllRequestsForItems(getActivationType(), items);
		ensureStates(requests);

		CALValidation validator = new CALValidation(holding, requests, activationService);
		Map<String, String> holdingAttrs = holding.getItem().getItemDefinition().getAttributes();
		validator.setPerCourseValidation(Boolean.valueOf(holdingAttrs.get(CALConstants.HAS_PERCOURSE_VALIDATION)));
		validator.setIgnoreOverrides(ignoreOverrides);
		validator.setSkipPercentage(skipPercentage);
		validator.setRestrictiveValidation(Boolean.valueOf(holdingAttrs.get(CALConstants.HAS_RESTRICTIVE_VALIDATION)));
		String percentage = holdingAttrs.get(CALConstants.KEY_PERCENTAGE_REQUIREMENT);
		if (percentage != null) {
			validator.setPercentageRequirement(Float.valueOf(percentage));
		}
		if( !validator.isValid() )
		{
			LanguageBundle calError = LangUtils.getBundleFromXmlString(holdingAttrs.get(CALConstants.ACTIVATIONERROR));
			CopyrightViolationException cve = new CopyrightViolationException(calError);
			cve.setCALBookPercentageException(validator.causedByBookPercentageException());
			throw cve;
		}
	}


	@Transactional
	@Override
	public String citate(CALHolding holding, CALPortion portion)
	{
		String citation = Constants.BLANK;

		Item item = holding.getItem();
		PropBagEx holdingXml = itemService.getItemXmlPropBag(item);
		PropBagEx copyrightXml = holdingXml.getSubtree("item/copyright"); //$NON-NLS-1$

		String type = holding.getType();

		if( type.equals(CALConstants.BOOK) )
		{
			citation = citationGen.citeBookPortion(holding, portion, copyrightXml, null);
		}
		else if( type.equals(CALConstants.JOURNAL) )
		{
			citation = citationGen.citeJournalPortion(holding, portion, copyrightXml, null);
		}

		return citation;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void validateItem(Item item, boolean ignoreOverrides, boolean skipPercentage)
	{
		validateHolding(getHoldingForItem(item), ignoreOverrides, skipPercentage);
	}

}
