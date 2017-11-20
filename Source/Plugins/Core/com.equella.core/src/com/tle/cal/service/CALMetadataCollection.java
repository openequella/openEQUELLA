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
import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.beans.item.Item;
import com.tle.beans.item.Relation;
import com.tle.cal.CALConstants;
import com.tle.cal.dao.CALDao;
import com.tle.common.Check;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemMetadataListener;
import com.tle.core.services.item.relation.RelationListener;
import com.tle.core.services.item.relation.RelationService;

@Bind
@Singleton
public class CALMetadataCollection implements ItemMetadataListener, RelationListener
{
	@Inject
	private RelationService relationService;
	@Inject
	private CALDao calDao;
	@Inject
	private CALService calService;

	@Override
	public void metadataChanged(Item item, PropBagEx itemXml)
	{
		PropBagEx copyright = itemXml.getSubtree(CALConstants.XML_CAL_ROOT);
		calDao.deleteAllForItem(item);
		if( copyright == null || !calService.isCopyrightedItem(item) )
		{
			return;
		}
		CALHolding holding = extractHolding(copyright);
		if( holding != null )
		{
			calDao.saveHolding(item, holding);
			Collection<Relation> portionRelations = relationService.getAllByToItemAndType(item,
				CALConstants.CAL_HOLDING);
			List<Item> portionItems = new ArrayList<Item>();
			for( Relation relation : portionRelations )
			{
				portionItems.add(relation.getFirstItem());
			}
			calDao.updateHoldingReference(holding, portionItems);
		}

		if( holding == null )
		{
			Collection<Relation> relation = relationService.getAllByFromItemAndType(item, CALConstants.CAL_HOLDING);
			if( !relation.isEmpty() )
			{
				Relation rel1 = relation.iterator().next();
				holding = calDao.getHoldingInItem(rel1.getSecondItem());
			}
		}

		List<CALPortion> portions = extractPortions(copyright);
		if( !portions.isEmpty() )
		{
			calDao.savePortions(item, holding, portions);
		}
		if( holding != null )
		{
			calService.validateHolding(holding, true, false);
		}
	}

	private List<CALPortion> extractPortions(PropBagEx copyright)
	{
		List<CALPortion> portions = new ArrayList<CALPortion>();
		PropBagThoroughIterator iter = copyright.iterateAll(CALConstants.XML_PORTION);
		while( iter.hasNext() )
		{
			PropBagEx portionXml = iter.next();
			CALPortion portion = new CALPortion();
			ListAndSeperated authors = getListAndSeperated(CALConstants.XML_AUTHOR, portionXml);
			portion.setAuthorList(authors.seperated);
			portion.setAuthors(authors.list);
			portion.setTitle(portionXml.getNode("title")); //$NON-NLS-1$
			portion.setChapter(portionXml.getNode("number")); //$NON-NLS-1$
			ListAndSeperated topics = getListAndSeperated(CALConstants.XML_TOPIC, portionXml);
			portion.setTopics(topics.list);
			PropBagThoroughIterator sectIter = portionXml.iterateAll(CALConstants.XML_SECTION);
			List<CALSection> sections = new ArrayList<CALSection>();
			while( sectIter.hasNext() )
			{
				PropBagEx sectionXml = sectIter.next();
				CALSection section = new CALSection();
				section.setPortion(portion);
				section.setRange(sectionXml.getNode("pages")); //$NON-NLS-1$
				String copyrightStatus = sectionXml.getNode("copyrightstatus");//$NON-NLS-1$
				section.setCopyrightStatus(Check.isEmpty(copyrightStatus) ? CALConstants.CAL_COPYRIGHTSTATUS
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

	private CALHolding extractHolding(PropBagEx copyright)
	{
		CALHolding holding = new CALHolding();
		String type = copyright.getNode("@type").toLowerCase(); //$NON-NLS-1$
		if( Check.isEmpty(type) )
		{
			return null;
		}
		holding.setType(type);
		holding.setOutOfPrint(copyright.isNodeTrue("outofprint")); //$NON-NLS-1$
		holding.setTitle(copyright.getNode("title")); //$NON-NLS-1$
		holding.setPublisher(copyright.getNode("publisher")); //$NON-NLS-1$
		ListAndSeperated authors = getListAndSeperated(CALConstants.XML_AUTHOR, copyright);
		holding.setAuthorList(authors.seperated);
		holding.setAuthors(authors.list);
		if( type.equals("book") ) //$NON-NLS-1$
		{
			processBook(holding, copyright);
		}
		else if( type.equals("journal") ) //$NON-NLS-1$
		{
			processJournal(holding, copyright);
		}
		else
		{
			return null;
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

	private void processBook(CALHolding holding, PropBagEx copyright)
	{
		processIds(holding, "isbn", copyright); //$NON-NLS-1$
		populatePublicationDate(holding, copyright);
		holding.setLength(copyright.getNode("pages")); //$NON-NLS-1$
	}

	private void processIds(CALHolding holding, String path, PropBagEx copyright)
	{
		ListAndSeperated ids = getListAndSeperated(path, copyright);
		holding.setIds(ids.list);
		holding.setIdList(ids.seperated);
	}

	private void processJournal(CALHolding holding, PropBagEx copyright)
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

	private void populatePublicationDate(CALHolding holding, PropBagEx copyright)
	{
		holding.setPubDate(copyright.getNode("publication/year")); //$NON-NLS-1$
	}

	@Override
	public void relationCreated(Relation relation)
	{
		CALHolding holding = calDao.getHoldingInItem(relation.getSecondItem());
		if( holding != null )
		{
			calDao.updateHoldingReference(holding, Collections.singletonList(relation.getFirstItem()));
			calService.validateHolding(holding, true, false);
		}
	}

	@Override
	public void relationDeleted(Relation relation)
	{
		CALHolding holding = calDao.getHoldingInItem(relation.getSecondItem());
		if( holding != null )
		{
			calDao.updateHoldingReference(null, Collections.singletonList(relation.getFirstItem()));
		}
	}
}
