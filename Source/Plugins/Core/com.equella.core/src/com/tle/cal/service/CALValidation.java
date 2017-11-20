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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Objects;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.beans.item.Item;
import com.tle.cal.CALConstants;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.activation.validation.ActivationPeriodHelper;
import com.tle.core.activation.validation.PageCounter;
import com.tle.core.activation.validation.PageCounter.RangeCounter;

public class CALValidation
{
	private static final String NO_NUMBER = "none"; //$NON-NLS-1$

	private static Log LOGGER = LogFactory.getLog(CALValidation.class);

	private ActivationService activationService;

	private final CALHolding holding;
	private final Set<String> active;
	private boolean perCourseValidation;
	private boolean restrictiveValidation;
	private float percentageRequirement;
	private List<ActivateRequest> allRequests;
	private boolean bookPercentageException = false;
	private boolean ignoreOverrides = false;
	private boolean skipPercentage = false;

	public CALValidation(CALHolding holding, List<ActivateRequest> allRequests, ActivationService activationService)
	{
		this.holding = holding;
		this.allRequests = allRequests;
		this.setPercentageRequirement(10);
		this.activationService = activationService;
		this.active = new HashSet<String>();
	}

	public void addActive(ActivateRequest request)
	{
		active.add(request.getItem().getId() + "_" + request.getAttachment()); //$NON-NLS-1$}
	}

	public boolean isValid()
	{
		if( holding.isOutOfPrint() )
		{
			return true;
		}

		HashMap<String, List<ActivateRequest>> requestsMap = new HashMap<String, List<ActivateRequest>>();
		if( perCourseValidation )
		{
			for( ActivateRequest request : allRequests )
			{
				String courseId = request.getCourse().getCode();
				List<ActivateRequest> activityList = requestsMap.get(courseId);
				if( activityList == null )
				{
					activityList = new ArrayList<ActivateRequest>();
					requestsMap.put(courseId, activityList);
				}
				activityList.add(request);
			}
		}
		else
		{
			requestsMap.put("", allRequests); //$NON-NLS-1$
		}

		Collection<List<ActivateRequest>> requests = requestsMap.values();
		for( List<ActivateRequest> list : requests )
		{
			ActivationPeriodHelper helper = new ActivationPeriodHelper(list);
			for( Date time : helper.calculatePoints() )
			{
				active.clear();
				List<ActivateRequest> intersects = helper.calculateIntersections(time);
				for( ActivateRequest request : intersects )
				{
					if( request.getStatus() != ActivateRequest.TYPE_INACTIVE )
					{
						addActive(request);
					}
				}

				boolean valid;
				if( holding.getType().equals(CALConstants.BOOK) )
				{
					valid = validateBook(holding);
				}
				else
				{
					valid = validateJournal(holding);
				}
				if( !valid )
				{
					return false;
				}
			}
		}

		return true;

	}

	private boolean validateJournal(CALHolding journal)
	{
		Collection<CALPortion> portions = journal.getCALPortions();
		if( portions != null )
		{
			Set<String> commonTopics = null;
			int activeArticles = 0;
			for( CALPortion article : portions )
			{
				List<CALSection> sections = article.getCALSections();
				boolean articleIsActive = false;
				for( CALSection section : sections )
				{
					String copyrightStatus = section.getCopyrightStatus();
					if( (copyrightStatus == null || copyrightStatus.equals("copyright")) //$NON-NLS-1$
						&& isActive(section) )
					{
						articleIsActive = true;
						break;
					}
				}
				if( articleIsActive )
				{
					activeArticles++;
					Set<String> topics = new HashSet<String>();
					List<String> aTopics = article.getTopics();
					if( aTopics != null )
					{
						topics.addAll(aTopics);
					}
					if( commonTopics == null )
					{
						commonTopics = topics;
					}
					else
					{
						for( Iterator<String> iter = topics.iterator(); iter.hasNext(); )
						{
							String topic = iter.next();
							if( !commonTopics.contains(topic) )
							{
								iter.remove();
							}
						}
						commonTopics = topics;
					}
				}
			}
			if( activeArticles > 1 && (commonTopics != null && commonTopics.size() == 0) )
			{
				LOGGER.warn("More than one article active, with no common topic"); //$NON-NLS-1$
				return false;
			}
		}
		return true;
	}

	private boolean validateBook(CALHolding book)
	{
		Collection<CALPortion> portions = book.getCALPortions();
		if( portions != null )
		{
			int totalPages = PageCounter.countTotalPages(book.getLength());
			if( totalPages == 0 )
			{
				return true;
			}
			RangeCounter counter = new RangeCounter();
			Set<String> activeChapterSet = new HashSet<String>();
			boolean forcePercentage = false;
			for( CALPortion chapter : portions )
			{
				boolean hasSomeActive = false;
				List<CALSection> sections = chapter.getCALSections();
				for( CALSection section : sections )
				{
					String copyrightStatus = section.getCopyrightStatus();
					if( (copyrightStatus == null || copyrightStatus.equals("copyright")) //$NON-NLS-1$
						&& isActive(section) )
					{
						if( !(ignoreOverrides && hasOverrideMessage(section)) )
						{
							PageCounter.processRange(section.getRange(), counter);
							hasSomeActive = true;
						}
					}
				}
				String chapterField = chapter.getChapter();
				if( hasSomeActive )
				{
					if( chapterField == null || chapterField.equals(NO_NUMBER) )
					{
						forcePercentage |= chapterField == null;
						chapterField = UUID.randomUUID().toString();
					}
					activeChapterSet.add(chapterField);
				}
			}
			if( activeChapterSet.size() > 1 || forcePercentage )
			{
				if( restrictiveValidation && activeChapterSet.size() > 1 )
				{
					LOGGER
						.warn("You cannot activate more than one copyrighted portion at the same time (restrictive validation is on)"); //$NON-NLS-1$
					return false;
				}
				if( !skipPercentage )
				{
					int allActivePages = counter.getTotal();
					double activePercent = (allActivePages / (double) totalPages) * 100;
					if( activePercent > this.percentageRequirement )
					{
						LOGGER
							.warn("Violation due to more than " + this.percentageRequirement + "% active: " + allActivePages + " of " //$NON-NLS-1$ //$NON-NLS-2$
								+ totalPages + " (force10:" + forcePercentage + ")"); //$NON-NLS-1$//$NON-NLS-2$
						this.bookPercentageException = true;
						return false;
					}
				}

			}
		}
		return true;
	}

	private boolean hasOverrideMessage(CALSection section)
	{
		Item item = section.getPortion().getItem();
		for( ActivateRequest request : activationService.getAllRequests(item) )
		{
			if( Objects.equal(request.getAttachment(), section.getAttachment()) )
			{
				if( request.getOverrideReason() != null )
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean isActive(CALSection section)
	{
		return active.contains(section.getPortion().getItem().getId() + "_" //$NON-NLS-1$
			+ section.getAttachment());
	}

	public void setPerCourseValidation(boolean perCourseValidation)
	{
		this.perCourseValidation = perCourseValidation;
	}

	public void setRestrictiveValidation(boolean restrictiveValidation)
	{
		this.restrictiveValidation = restrictiveValidation;
	}

	public void setPercentageRequirement(float percentageRequirement)
	{
		this.percentageRequirement = percentageRequirement;
	}

	public boolean causedByBookPercentageException()
	{
		return bookPercentageException;
	}

	public void setIgnoreOverrides(boolean ignoreOverrides)
	{
		this.ignoreOverrides = ignoreOverrides;
	}

	public void setSkipPercentage(boolean skipPercentage)
	{
		this.skipPercentage = skipPercentage;
	}

}
