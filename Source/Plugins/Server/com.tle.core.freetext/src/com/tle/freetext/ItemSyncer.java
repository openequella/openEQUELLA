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

package com.tle.freetext;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.TermQuery;

import com.dytech.edge.common.valuebean.ItemIndexDate;
import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.searching.Search;
import com.tle.core.freetext.index.AbstractIndexEngine.Searcher;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.dao.ItemDao;

public class ItemSyncer implements Callable<Void>
{
	private static final int BATCH_INDEXSYNC = 1000;
	private static final Logger LOGGER = Logger.getLogger(ItemSyncer.class);

	@Inject
	private ItemDao itemDao;
	@Inject
	private FreetextIndex freetextIndex;
	@Inject
	private IndexedItemFactory indexedItemFactory;

	private final Collection<Institution> institutions;
	private final Date afterDate;
	private final Map<Long, Institution> instMap = Maps.newHashMap();
	private final BackgroundIndexer backgroundIndexer;
	private List<ItemIndexDelete> toDelete;

	@AssistedInject
	protected ItemSyncer(@Assisted Collection<Institution> institutions, @Assisted @Nullable Date afterDate,
		@Assisted BackgroundIndexer backgroundIndexer)
	{
		this.institutions = institutions;
		this.backgroundIndexer = backgroundIndexer;
		if( afterDate == null )
		{
			toDelete = Lists.newArrayList();
			afterDate = new Date(0L);
		}
		this.afterDate = afterDate;
		for( Institution institution : institutions )
		{
			instMap.put(institution.getUniqueId(), institution);
		}
	}

	@SuppressWarnings("nls")
	@Override
	public Void call() throws Exception
	{
		Date startTime = new Date();
		Pair<Long, Long> idRange = itemDao.getIdRange(institutions, afterDate);
		final long firstId;
		final long lastId;
		if( idRange == null )
		{
			if( toDelete == null )
			{
				return null;
			}
			firstId = 0;
			lastId = Long.MAX_VALUE;
		}
		else
		{
			firstId = idRange.getFirst();
			lastId = idRange.getSecond();
		}
		long rangeFirstId = firstId;
		List<ItemIndexDate> indexedTimes;
		do
		{
			indexedTimes = itemDao.getIndexTimesFromId(institutions, afterDate, rangeFirstId, lastId, BATCH_INDEXSYNC);
			long lastInRange;
			if( indexedTimes.isEmpty() )
			{
				lastInRange = lastId;
			}
			else
			{
				lastInRange = indexedTimes.get(indexedTimes.size() - 1).getKey().getKey();
			}
			final CompareDateCollector compareDates = new CompareDateCollector(indexedTimes, indexedItemFactory,
				instMap, toDelete, startTime);
			Collection<IndexedItem> changes = freetextIndex.getIndexer(Search.INDEX_ITEM)
				.search(new CompareSearcher(institutions, compareDates, rangeFirstId, lastInRange));
			if( !changes.isEmpty() )
			{
				waitForRoom(changes);
			}
			rangeFirstId = lastInRange + 1;
		}
		while( indexedTimes.size() == BATCH_INDEXSYNC );
		if( toDelete != null )
		{
			DatesBeforeCollector datesBefore = new DatesBeforeCollector(instMap, toDelete, startTime);
			int beforeCount = toDelete.size();
			if( firstId > 0 )
			{
				freetextIndex.getIndexer(Search.INDEX_ITEM)
					.search(new CompareSearcher(institutions, datesBefore, 0, firstId - 1));
			}
			int beforeCount2 = toDelete.size();
			if( lastId != Long.MAX_VALUE )
			{
				freetextIndex.getIndexer(Search.INDEX_ITEM)
					.search(new CompareSearcher(institutions, datesBefore, lastId + 1, Long.MAX_VALUE));
			}
			if( toDelete.size() != beforeCount )
			{
				LOGGER.info("Found " + (toDelete.size() - beforeCount2) + " items after id range and "
					+ (beforeCount2 - beforeCount) + " before range");
			}
		}

		if( !Check.isEmpty(toDelete) )
		{
			LOGGER.info("Found " + toDelete.size() + " items in index but not in db");
			List<IndexedItem> deletedDocs = Lists.newArrayList();
			for( ItemIndexDelete deletedItem : toDelete )
			{
				IndexedItem delete = indexedItemFactory.create(new ItemIdKey(deletedItem.getItemId(), "<DELETED>", 1),
					deletedItem.getInstitution());
				deletedDocs.add(delete);
				if( deletedDocs.size() >= BATCH_INDEXSYNC )
				{
					waitForRoom(deletedDocs);
					deletedDocs.clear();
				}
			}
			waitForRoom(deletedDocs);
		}
		return null;
	}

	private void waitForRoom(Collection<IndexedItem> changes) throws InterruptedException
	{
		while( !backgroundIndexer.isRoomForItems(changes.size()) )
		{
			Thread.sleep(2000);
		}
		backgroundIndexer.addAllToQueue(changes);
	}

	private static class CompareSearcher implements Searcher<Collection<IndexedItem>>
	{
		private final Collection<Institution> institutions;
		private final AbstractCompareDateCollector compareDates;
		private final long firstId;
		private final long lastId;

		public CompareSearcher(Collection<Institution> institutions, AbstractCompareDateCollector compareDates,
			long firstId, long lastId)
		{
			this.institutions = institutions;
			this.compareDates = compareDates;
			this.firstId = firstId;
			this.lastId = lastId;
		}

		@Override
		public Collection<IndexedItem> search(IndexSearcher searcher) throws IOException
		{
			BooleanQuery overall = new BooleanQuery();
			BooleanQuery collections = new BooleanQuery();
			for( Institution inst : institutions )
			{
				collections.add(
					new TermQuery(new Term(FreeTextQuery.FIELD_INSTITUTION, Long.toString(inst.getUniqueId()))),
					Occur.SHOULD);
			}
			overall.add(collections, Occur.MUST);
			overall.add(NumericRangeQuery.newLongRange(FreeTextQuery.FIELD_ID_RANGEABLE, firstId, lastId, true, true),
				Occur.MUST);
			searcher.search(overall, compareDates);
			return compareDates.getModifiedDocs();
		}
	}

	@BindFactory
	public interface ItemSyncFactory
	{
		ItemSyncer create(Collection<Institution> institutions, Date afterDate, BackgroundIndexer backgroundIndexer);
	}
}
