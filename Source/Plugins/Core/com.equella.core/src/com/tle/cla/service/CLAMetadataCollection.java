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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagThoroughIterator;
import com.dytech.devlib.PropBagEx.ValueIterator;
import com.tle.beans.cla.CLAHolding;
import com.tle.beans.cla.CLAPortion;
import com.tle.beans.cla.CLASection;
import com.tle.beans.item.Item;
import com.tle.beans.item.Relation;
import com.tle.cla.CLAConstants;
import com.tle.cla.dao.CLADao;
import com.tle.common.Check;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.activation.validation.PageCounter;
import com.tle.core.activation.validation.PageCounter.RangeCounter;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemMetadataListener;
import com.tle.core.services.item.relation.RelationListener;
import com.tle.core.services.item.relation.RelationService;

@Bind
@Singleton
public class CLAMetadataCollection implements ItemMetadataListener, RelationListener
{

	@Inject
	private RelationService relationService;
	@Inject
	private CLADao claDao;
	@Inject
	private CLAService claService;

	@Override
	public void metadataChanged(Item item, PropBagEx itemXml)
	{
		PropBagEx copyright = itemXml.getSubtree(CLAConstants.XML_CLA_ROOT);
		claDao.deleteAllForItem(item);
		if( copyright == null || !claService.isCopyrightedItem(item) )
		{
			return;
		}
		CLAHolding holding = extractHolding(copyright);
		if( holding != null )
		{
			claDao.saveHolding(item, holding);
			Collection<Relation> portionRelations = relationService.getAllByToItemAndType(item, CLAConstants.HOLDING);
			List<Item> portionItems = new ArrayList<Item>();
			for( Relation relation : portionRelations )
			{
				portionItems.add(relation.getFirstItem());
			}
			claDao.updateHoldingReference(holding, portionItems);
		}

		if( holding == null )
		{
			Collection<Relation> relation = relationService.getAllByFromItemAndType(item, CLAConstants.HOLDING);
			if( !relation.isEmpty() )
			{
				Relation rel1 = relation.iterator().next();
				holding = claDao.getHoldingInItem(rel1.getSecondItem());
			}
		}

		List<CLAPortion> portions = extractPortions(copyright);
		if( !portions.isEmpty() )
		{
			claDao.savePortions(item, holding, portions);
		}
		if( holding != null )
		{
			claService.validateHolding(holding);
		}
	}

	private List<CLAPortion> extractPortions(PropBagEx copyright)
	{
		List<CLAPortion> portions = new ArrayList<CLAPortion>();
		PropBagThoroughIterator iter = copyright.iterateAll(CLAConstants.XML_PORTION);
		while( iter.hasNext() )
		{
			PropBagEx portionXml = iter.next();
			CLAPortion portion = new CLAPortion();
			ListAndSeperated authors = getListAndSeperated(CLAConstants.XML_AUTHOR, portionXml);
			portion.setAuthorList(authors.seperated);
			portion.setAuthors(authors.list);
			portion.setTitle(portionXml.getNode("title")); //$NON-NLS-1$
			portion.setChapter(portionXml.getNode("number")); //$NON-NLS-1$
			ListAndSeperated topics = getListAndSeperated(CLAConstants.XML_TOPIC, portionXml);
			portion.setTopics(topics.list);
			doCLASpecific(portion, portionXml);
			PropBagThoroughIterator sectIter = portionXml.iterateAll(CLAConstants.XML_SECTION);
			List<CLASection> sections = new ArrayList<CLASection>();
			while( sectIter.hasNext() )
			{
				PropBagEx sectionXml = sectIter.next();
				CLASection section = new CLASection();
				section.setPortion(portion);
				RangeCounter rangeCounter = new RangeCounter();
				String pageRange = sectionXml.getNode("pages"); //$NON-NLS-1$
				section.setRange(pageRange);
				PageCounter.processRange(pageRange, rangeCounter);
				section.setRangeStart(rangeCounter.getRangeStart());
				section.setRangeEnd(rangeCounter.getRangeEnd());
				String copyrightStatus = sectionXml.getNode("copyrightstatus");//$NON-NLS-1$
				section.setCopyrightStatus(Check.isEmpty(copyrightStatus) ? CLAConstants.COPYRIGHTSTATUS
					: copyrightStatus);
				section.setAttachment(sectionXml.getNode("attachment", null)); //$NON-NLS-1$
				section.setIllustration(sectionXml.getNode("illustration").length() > 0); //$NON-NLS-1$
				sections.add(section);
			}
			portion.setSections(sections);
			portions.add(portion);
		}
		return portions;
	}

	private void doCLASpecific(CLAPortion portion, PropBagEx portionXml)
	{
		portion.setSource(firstChar(portionXml.getNode("cla/source"))); //$NON-NLS-1$
		portion.setReason(firstChar(portionXml.getNode("cla/reason"))); //$NON-NLS-1$
		portion.setArtisticWorks(firstChar(portionXml.getNode("cla/artistic"))); //$NON-NLS-1$
		portion.setSourceInstitution(portionXml.getNode("cla/source_institution")); //$NON-NLS-1$
	}

	private Character firstChar(String val)
	{
		if( val.length() == 0 )
		{
			return null;
		}
		return val.charAt(0);
	}

	private CLAHolding extractHolding(PropBagEx copyright)
	{
		CLAHolding holding = new CLAHolding();
		String type = copyright.getNode("@type").toLowerCase(); //$NON-NLS-1$
		if( Check.isEmpty(type) )
		{
			return null;
		}
		holding.setType(type);
		holding.setOutOfPrint(copyright.isNodeTrue("outofprint")); //$NON-NLS-1$
		holding.setTitle(copyright.getNode("title")); //$NON-NLS-1$
		holding.setPublisher(copyright.getNode("publisher")); //$NON-NLS-1$
		ListAndSeperated authors = getListAndSeperated(CLAConstants.XML_AUTHOR, copyright);
		holding.setAuthorList(authors.seperated);
		holding.setAuthors(authors.list);

		if( type.equals("journal") ) //$NON-NLS-1$
		{
			processJournal(holding, copyright);
		}
		else
		{
			processBook(holding, copyright);
		}
		return holding;
	}

	public static class ListAndSeperated
	{
		String seperated;
		List<String> list;
	}

	private ListAndSeperated getListAndSeperated(String path, PropBagEx xml)
	{
		ListAndSeperated ret = new ListAndSeperated();
		List<String> list = new ArrayList<String>();
		StringBuilder sbuf = new StringBuilder();
		ValueIterator iter = xml.iterateValues(path);
		boolean first = true;
		while( iter.hasNext() )
		{
			String val = iter.next();
			if( !first )
			{
				sbuf.append("; "); //$NON-NLS-1$
			}
			sbuf.append(val);
			list.add(val);
			first = false;
		}
		ret.list = list;
		ret.seperated = sbuf.toString();
		return ret;
	}

	private void processBook(CLAHolding holding, PropBagEx copyright)
	{
		processIds(holding, "isbn", copyright); //$NON-NLS-1$
		populatePublicationDate(holding, copyright);
		holding.setLength(copyright.getNode("pages")); //$NON-NLS-1$
	}

	private void processIds(CLAHolding holding, String path, PropBagEx copyright)
	{
		ListAndSeperated ids = getListAndSeperated(path, copyright);
		holding.setIds(ids.list);
		holding.setIdList(ids.seperated);
	}

	private void processJournal(CLAHolding holding, PropBagEx copyright)
	{
		processIds(holding, "issn", copyright); //$NON-NLS-1$
		populatePublicationDate(holding, copyright);
		holding.setVolume(copyright.getNode("volume")); //$NON-NLS-1$
		String issue = copyright.getNode("issue/value"); //$NON-NLS-1$
		try
		{
			Date issueDate = new UtcDate(issue, Dates.ISO_MIDNIGHT).toDate();
			holding.setIssueDate(issueDate);
		}
		catch( ParseException pe )
		{
			holding.setIssueNumber(issue);
		}
	}

	private void populatePublicationDate(CLAHolding holding, PropBagEx copyright)
	{
		holding.setPubDate(copyright.getNode("publication/year")); //$NON-NLS-1$
	}

	@Override
	public void relationCreated(Relation relation)
	{
		CLAHolding holding = claDao.getHoldingInItem(relation.getSecondItem());
		if( holding != null )
		{
			claDao.updateHoldingReference(holding, Collections.singletonList(relation.getFirstItem()));
			claService.validateHolding(holding);
		}
	}

	@Override
	public void relationDeleted(Relation relation)
	{
		CLAHolding holding = claDao.getHoldingInItem(relation.getSecondItem());
		if( holding != null )
		{
			claDao.updateHoldingReference(null, Collections.singletonList(relation.getFirstItem()));
		}
	}

}
