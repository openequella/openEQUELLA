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

package com.tle.cla.web.viewitem.summary;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.cla.CLAHolding;
import com.tle.beans.cla.CLAPortion;
import com.tle.beans.cla.CLASection;
import com.tle.cla.CLAConstants;
import com.tle.cla.web.service.CLAWebServiceImpl;
import com.tle.core.activation.validation.PageCounter;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.section.AbstractActivateSection;
import com.tle.web.copyright.section.AbstractCopyrightSummarySection;
import com.tle.web.copyright.service.CopyrightWebService;

@NonNullByDefault
@Bind
public class CLASummarySection extends AbstractCopyrightSummarySection<CLAHolding, CLAPortion, CLASection>
{
	@Inject
	private CLAWebServiceImpl claWebService;

	@Override
	protected Class<? extends AbstractActivateSection> getActivateSectionClass()
	{
		return CLAActivateSection.class;
	}

	@Override
	protected HoldingDisplay createHoldingDisplay(Holding holding)
	{
		HoldingDisplay holdingDisplay = new HoldingDisplay();
		boolean book = holding.getType().equalsIgnoreCase(CLAConstants.BOOK);
		if( book )
		{
			holdingDisplay.setBook(true);
		}
		holdingDisplay.setShowPages(true);
		holdingDisplay.setTotalPages(PageCounter.countTotalPages(holding.getLength()));
		return holdingDisplay;
	}

	@Override
	protected String getChapterName(HoldingDisplay holdingDisplay, Portion portion)
	{
		return portion.getChapter();
	}

	@Override
	protected String getPortionId(HoldingDisplay holdingDisplay, Portion portion)
	{
		String chapter = portion.getChapter();
		if( chapter == null )
		{
			return Long.toString(portion.getId());
		}
		return chapter;
	}

	@Override
	protected CopyrightWebService<CLAHolding> getCopyrightWebServiceImpl()
	{
		return claWebService;
	}

	@Override
	protected CopyrightService<CLAHolding, CLAPortion, CLASection> getCopyrightServiceImpl()
	{
		return claWebService.getCopyrightServiceImpl();
	}

	@Override
	protected void processAvailablePages(Holding holding, HoldingDisplay holdingDisplay)
	{
		double percent = CLAConstants.PERCENTAGE;
		int totalPages = holdingDisplay.getTotalPages();

		int activatablePages = (int) (holding.getType() == CLAConstants.TYPE_STORIES ? CLAConstants.MAX_STORY_PAGES
			: (percent / 100) * totalPages);

		int activePages = (int) ((holdingDisplay.getTotalActivePercent() / 100) * totalPages);
		int pagesLeft = activatablePages - activePages;

		int totalInactive = (int) ((holdingDisplay.getTotalInactivePercent() / 100) * totalPages);

		holdingDisplay.setPagesAvailable(pagesLeft > totalInactive ? totalInactive : pagesLeft);
	}
}
