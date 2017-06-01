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

package com.tle.cal.web.viewitem.summary;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.jsoup.Jsoup;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.cal.CALConstants;
import com.tle.cal.service.CALService;
import com.tle.cal.web.service.CALWebServiceImpl;
import com.tle.core.activation.validation.PageCounter;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.section.AbstractActivateSection;
import com.tle.web.copyright.section.AbstractCopyrightSummarySection;
import com.tle.web.copyright.service.CopyrightWebService;
import com.tle.web.sections.render.TextLabel;

@NonNullByDefault
@Bind
public class CALSummarySection extends AbstractCopyrightSummarySection<CALHolding, CALPortion, CALSection>
{
	@Inject
	private CALWebServiceImpl calWebService;

	@Override
	protected Class<? extends AbstractActivateSection> getActivateSectionClass()
	{
		return CALActivateSection.class;
	}

	@Override
	protected CopyrightService<CALHolding, CALPortion, CALSection> getCopyrightServiceImpl()
	{
		return calWebService.getCopyrightServiceImpl();
	}

	@Override
	protected CopyrightWebService<CALHolding> getCopyrightWebServiceImpl()
	{
		return calWebService;
	}

	@Override
	protected HoldingDisplay createHoldingDisplay(Holding holding)
	{
		HoldingDisplay display = new HoldingDisplay();
		boolean book = holding.getType().equalsIgnoreCase(CALConstants.BOOK);
		if( book )
		{
			display.setBook(true);
			display.setShowPages(true);
			display.setTotalPages(PageCounter.countTotalPages(holding.getLength()));
		}
		return display;
	}

	@Override
	protected double setupPageRange(HoldingDisplay holdingDisplay, SectionDisplay sectionDisplay,
		com.tle.core.copyright.Section section)
	{
		if( holdingDisplay.isBook() )
		{
			return super.setupPageRange(holdingDisplay, sectionDisplay, section);
		}
		return 0;
	}

	@Override
	protected String getPortionId(HoldingDisplay holdingDisplay, Portion portion)
	{
		if( holdingDisplay.isBook() )
		{
			String chapter = portion.getChapter();
			if( chapter != null && !chapter.equals("none") ) //$NON-NLS-1$
			{
				return chapter;
			}
			else
			{
				return UUID.randomUUID().toString();
			}
		}
		return portion.getTitle();
	}

	@Override
	protected String getChapterName(HoldingDisplay holdingDisplay, Portion portion)
	{
		if( holdingDisplay.isBook() )
		{
			String chapter = portion.getChapter();
			if( chapter != null && !chapter.equals("none") ) //$NON-NLS-1$
			{
				return chapter;
			}
			return null;
		}
		return portion.getChapter();
	}

	@Override
	protected TextLabel getAttachmentDisplayName(Attachment attachment)
	{
		Item item = attachment.getItem();
		Map<String, String> attributes = item.getItemDefinition().getAttributes();
		if( Boolean.valueOf(attributes.get(CALConstants.KEY_USE_CITATION_AS_NAME)) )
		{
			CALService calService = (CALService) getCopyrightServiceImpl();
			CALHolding holding = calService.getHoldingForItem(item);
			Map<Long, List<CALPortion>> portions = calService.getPortionsForItems(Collections.singletonList(item));
			CALPortion calPortion = portions.get(item.getId()) == null ? null : portions.get(item.getId()).get(0);
			String citation = calService.citate(holding, calPortion);
			return new TextLabel(Jsoup.parse(citation).text());
		}
		return new TextLabel(attachment.getDescription());
	}

	@Override
	protected void processAvailablePages(Holding holding, HoldingDisplay holdingDisplay)
	{
		Map<String, String> holdingAttrs = holding.getItem().getItemDefinition().getAttributes();
		String percentage = holdingAttrs.get(CALConstants.KEY_PERCENTAGE_REQUIREMENT);
		Float percentageLimit = percentage == null ? 10 : Float.valueOf(percentage);
		int totalPages = holdingDisplay.getTotalPages();

		int activatablePages = (int) ((percentageLimit / 100) * totalPages);
		int activePages = (int) ((holdingDisplay.getTotalActivePercent() / 100) * totalPages);
		int pagesLeft = activatablePages - activePages;
		pagesLeft = pagesLeft < 0 ? 0 : pagesLeft;
		int totalInactive = (int) ((holdingDisplay.getTotalInactivePercent() / 100) * totalPages);

		holdingDisplay.setPagesAvailable(pagesLeft > totalInactive ? totalInactive : pagesLeft);
	}
}
