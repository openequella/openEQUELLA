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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.cla.CLAHolding;
import com.tle.beans.cla.CLAPortion;
import com.tle.beans.cla.CLASection;
import com.tle.cla.CLAConstants;
import com.tle.core.activation.validation.PageCounter;
import com.tle.core.activation.validation.PageCounter.RangeCounter;

public class CLAValidation
{
	private static Log LOGGER = LogFactory.getLog(CLAValidation.class);

	private final CLAHolding holding;
	private Set<String> active;

	public CLAValidation(CLAHolding holding)
	{
		this.holding = holding;
		this.active = new HashSet<String>();
	}

	/**
	 * Resets ALL statuses to inactive and percents to zero.
	 */
	public void reset()
	{
		active.clear();
	}

	public void addActive(ActivateRequest request)
	{
		active.add(request.getItem().getId() + "_" + request.getAttachment()); //$NON-NLS-1$
	}

	public boolean isValid()
	{
		int maxPages = -1;
		if( holding.getType().equals(CLAConstants.TYPE_STORIES) )
		{
			maxPages = CLAConstants.MAX_STORY_PAGES;
		}
		return validateBook(holding, maxPages);
	}

	@SuppressWarnings("nls")
	private boolean validateBook(CLAHolding book, int maxPages)
	{
		Collection<CLAPortion> portions = book.getCLAPortions();
		if( portions != null )
		{
			int totalPages = PageCounter.countTotalPages(book.getLength());
			if( totalPages == 0 )
			{
				return true;
			}
			RangeCounter counter = new RangeCounter();
			Set<String> activeChapterSet = new HashSet<String>();
			for( CLAPortion chapter : portions )
			{
				boolean hasSomeActive = false;
				List<CLASection> sections = chapter.getCLASections();
				for( CLASection section : sections )
				{
					String copyrightStatus = section.getCopyrightStatus();
					if( (copyrightStatus == null || copyrightStatus.equals(CLAConstants.COPYRIGHT_STATUS))
						&& isActive(section) )
					{
						PageCounter.processRange(section.getRange(), counter);
						hasSomeActive = true;
					}
				}
				String chapterField = chapter.getChapter();
				if( hasSomeActive )
				{
					if( chapterField == null )
					{
						chapterField = UUID.randomUUID().toString();
					}
					activeChapterSet.add(chapterField);
				}
			}
			int allActivePages = counter.getTotal();
			if( maxPages != -1 && allActivePages > maxPages )
			{
				LOGGER.warn("Violation due to story or poem being longer than 10 pages");
				return false;
			}
			if( activeChapterSet.size() > 1 )
			{
				double activePercentage = (allActivePages / (double) totalPages) * 100;
				if( activePercentage > CLAConstants.PERCENTAGE )
				{
					LOGGER.warn(MessageFormat.format("Violation due to more than " + CLAConstants.PERCENTAGE
						+ "% active: {0} of {1} ", allActivePages, totalPages));
					return false;
				}
			}
		}
		return true;
	}

	public boolean isActive(CLASection section)
	{
		return active.contains(section.getPortion().getItem().getId() + "_" //$NON-NLS-1$
			+ section.getAttachment());
	}

}
