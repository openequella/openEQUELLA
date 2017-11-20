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

package com.tle.core.freetext.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Multimap;
import com.tle.beans.Institution;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.ItemIdKeySearchResults;
import com.tle.common.search.LiveItemSearch;
import com.tle.common.search.whereparser.WhereParser;
import com.tle.common.searching.Search;
import com.tle.common.searching.SearchResults;
import com.tle.core.freetext.event.ItemReindexEvent;
import com.tle.core.freetext.event.listener.ItemReindexListener;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.queries.FreeTextFieldQuery;
import com.tle.core.freetext.reindex.InstitutionFilter;
import com.tle.core.freetext.reindex.ReindexFilter;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.events.InstitutionEvent;
import com.tle.core.institution.events.listeners.InstitutionListener;
import com.tle.core.item.event.IndexItemBackgroundEvent;
import com.tle.core.item.event.IndexItemNowEvent;
import com.tle.core.item.event.UnindexItemEvent;
import com.tle.core.item.event.WaitForItemIndexEvent;
import com.tle.core.item.event.listener.IndexItemBackgroundListener;
import com.tle.core.item.event.listener.IndexItemNowListener;
import com.tle.core.item.event.listener.UnindexItemListener;
import com.tle.core.item.event.listener.WaitForItemIndexListener;
import com.tle.core.item.service.ItemService;
import com.tle.core.remoting.MatrixResults;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.services.item.StdFreetextResults;
import com.tle.freetext.BackgroundIndexer;
import com.tle.freetext.BackgroundIndexerImpl.BackgroundIndexerFactory;
import com.tle.freetext.FreetextIndex;
import com.tle.freetext.IndexedItem;

import it.uniroma3.mat.extendedset.wrappers.LongSet;

/**
 * @author Nicholas Read
 */
@Bind(FreeTextService.class)
@Singleton
@SuppressWarnings("nls")
public class FreeTextServiceImpl
	implements
		FreeTextService,
		ItemReindexListener,
		UnindexItemListener,
		IndexItemBackgroundListener,
		WaitForItemIndexListener,
		IndexItemNowListener,
		InstitutionListener
{

	private static final Logger LOGGER = Logger.getLogger(FreeTextServiceImpl.class);

	@Inject
	private FreetextIndex indexer;
	@Inject
	private ItemService itemService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private BackgroundIndexerFactory backgroundProvider;

	private Date lastSync = new Date();
	private final Timer timer = new Timer();
	private final LoadingCache<Long, BackgroundIndexer> backgroundIndexers = CacheBuilder.newBuilder()
		.build(CacheLoader.from(new CreateBackgroundIndexer()));

	private final ExecutorService backgroundExecutor = Executors.newCachedThreadPool(new ThreadFactory()
	{
		int count;

		@Override
		public Thread newThread(Runnable r)
		{
			count++;
			return new Thread(r, "Background indexer - " + count);
		}
	});

	@PostConstruct
	public void startSyncer()
	{
		long syncMillis = TimeUnit.MINUTES.toMillis(indexer.getSynchroniseMinutes());
		timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				Date thisTime = lastSync;
				lastSync = new Date();
				Collection<Entry<Long, BackgroundIndexer>> indexers = backgroundIndexers.asMap().entrySet();
				Multimap<Long, Institution> available = institutionService.getAvailableMap();
				for( Entry<Long, BackgroundIndexer> indexerEntry : indexers )
				{
					BackgroundIndexer backgroundIndexer = indexerEntry.getValue();
					Collection<Institution> insts = available.get(indexerEntry.getKey());
					if( !insts.isEmpty() )
					{
						backgroundIndexer.synchronizeNew(insts, thisTime);
					}
					// TODO could kill the indexers here
				}
			}
		}, syncMillis, syncMillis);
	}

	private BackgroundIndexer getBackgroundIndexer()
	{
		return backgroundIndexers.getUnchecked(institutionService.getSchemaIdForInstitution(CurrentInstitution.get()));
	}

	public class CreateBackgroundIndexer implements Function<Long, BackgroundIndexer>
	{
		@Override
		public BackgroundIndexer apply(Long schemaId)
		{
			BackgroundIndexer background = backgroundProvider.create(schemaId);
			backgroundExecutor.execute(background);
			return background;
		}
	}

	public void unindexDoc(ItemIdKey key)
	{
		LOGGER.info("Unindexing: " + key); //$NON-NLS-1$
		try
		{
			IndexedItem item = getBackgroundIndexer().createIndexedItem(key);
			item.setNewSearcherRequired(true);
			getBackgroundIndexer().addToQueue(item);
		}
		catch( Exception ex )
		{
			LOGGER.error("Error while trying to unindex document", ex); //$NON-NLS-1$
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void waitUntilIndexed(ItemIdKey itemIdKey)
	{
		IndexedItem inditem = getBackgroundIndexer().getIndexedItem(itemIdKey);
		if( inditem == null )
		{
			return;
		}
		while( !inditem.isIndexed() && !inditem.isErrored() )
		{
			inditem = getBackgroundIndexer().getIndexedItem(itemIdKey);
			if( inditem == null )
			{
				return;
			}
			synchronized( inditem )
			{
				if( inditem.isNoLongerCurrent() )
				{
					continue;
				}
				if( inditem.isIndexed() || inditem.isErrored() )
				{
					return;
				}
				try
				{
					inditem.wait();
				}
				catch( InterruptedException e )
				{
					// nothing
				}
			}
		}
		Throwable error = inditem.getError();
		if( error != null )
		{
			if( error instanceof RuntimeException )
			{
				throw (RuntimeException) error;
			}
			throw new RuntimeException(error);
		}
	}

	@Override
	public void indexItemNowEvent(IndexItemNowEvent event)
	{
		try
		{
			BackgroundIndexer backgroundIndexer = getBackgroundIndexer();
			IndexedItem inditem = backgroundIndexer.createIndexedItem(event.getItemIdKey());
			inditem.setExpectedReturnTime(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(2));
			inditem.setDeadlineAfterStart(false);
			inditem.setAdd(true);
			inditem.setNewSearcherRequired(true);
			backgroundIndexer.addToQueue(inditem);
		}
		catch( Exception ex )
		{
			LOGGER.error("Error while trying to index document", ex); //$NON-NLS-1$
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void waitForItem(WaitForItemIndexEvent event)
	{
		waitUntilIndexed(event.getItemIdKey());
	}

	/*
	 * (non-Javadoc)
	 * @seecom.tle.core.events.listeners.IndexItemBackgroundListener#
	 * indexItemBackgroundEvent(com.tle.core.events.IndexItemBackgroundEvent)
	 */
	@Override
	public void indexItemBackgroundEvent(IndexItemBackgroundEvent event)
	{
		getBackgroundIndexer().addToQueue(event.getItemIdKey(), true);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.events.listeners.ItemReindexListener#itemReindexEvent(com
	 * .tle.core.events.ItemReindexEvent)
	 */
	@Override
	public void itemReindexEvent(ItemReindexEvent event)
	{
		ReindexFilter filter = event.getFilter();
		filter.updateIndexTimes(itemService);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.services.FreeTextService#indexAll()
	 */
	@Override
	public void indexAll()
	{
		itemReindexEvent(new ItemReindexEvent(new InstitutionFilter()));
	}

	@Override
	public <T extends FreetextResult> FreetextSearchResults<T> search(Search searchreq, int nStart, int nCount)
	{
		SearchResults<T> fTextResults = indexer.search(searchreq, nStart, nCount);
		return new StdFreetextResults<T>(itemService, fTextResults, searchreq);
	}

	@Override
	public SearchResults<ItemIdKey> searchIds(Search searchreq, int nStart, int nCount)
	{
		SearchResults<FreetextResult> results = indexer.search(searchreq, nStart, nCount);
		return new ItemIdKeySearchResults(StdFreetextResults.convertToKeys(results.getResults()), results.getCount(),
			results.getOffset(), results.getAvailable());
	}

	@Override
	public LongSet searchIdsBitSet(Search searchreq)
	{
		return indexer.searchBitSet(searchreq);
	}

	@Override
	public int totalCount(Collection<String> collectionUuids, String where)
	{
		FreeTextQuery query = WhereParser.parse(where);

		DefaultSearch search = new DefaultSearch();
		search.setFreeTextQuery(query);
		search.setCollectionUuids(collectionUuids);

		return countsFromFilters(Collections.singletonList(search))[0];
	}

	@Override
	public int[] countsFromFilters(Collection<? extends Search> filters)
	{
		int[] res = new int[filters.size()];

		Date t1 = new Date();
		int rescount = 0;
		for( Search search : filters )
		{
			res[rescount++] = search != null ? indexer.count(search) : -1;
		}
		Date t2 = new Date();

		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("Total time taken: " + (t2.getTime() - t1.getTime()) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return res;
	}

	@Override
	public void unindexItemEvent(UnindexItemEvent event)
	{
		unindexDoc(event.getItemId());
	}

	@Override
	public List<ItemIdKey> getKeysForNodeValue(String uuid, ItemDefinition itemDef, String node, String value)
	{
		DefaultSearch search = new LiveItemSearch();
		if( itemDef != null )
		{
			search.setCollectionUuids(Collections.singleton(itemDef.getUuid()));
		}

		FreeTextQuery query = new FreeTextFieldQuery(node, value, true);

		// value AND NOT uuid
		if( uuid != null )
		{
			FreeTextQuery uq = new FreeTextFieldQuery(FreeTextQuery.FIELD_UUID, uuid, true);
			FreeTextQuery nuq = new FreeTextBooleanQuery(true, false, uq);

			query = new FreeTextBooleanQuery(false, true, query, nuq);
		}

		search.setFreeTextQuery(query);

		return searchIds(search, 0, Integer.MAX_VALUE).getResults();
	}

	@Override
	public void institutionEvent(final InstitutionEvent event)
	{
		switch( event.getEventType() )
		{
			case AVAILABLE:
				Multimap<Long, Institution> schema2inst = event.getChanges();
				Set<Long> schemas = schema2inst.keySet();
				for( Long schemaId : schemas )
				{
					backgroundIndexers.getUnchecked(schemaId).synchronizeFull(schema2inst.get(schemaId));
				}
				break;
			default:
				break;
		}

	}

	@Override
	public List<ItemIdKey> getAutoCompleteTitles(Search request)
	{
		return searchIds(request, 0, 4).getResults();
	}

	@Override
	public String getAutoCompleteTerm(Search request, String prefix)
	{
		String suggestedTerm = indexer.suggestTerm(request, prefix);
		if( !suggestedTerm.equals(prefix) && !Check.isEmpty(suggestedTerm) )
		{
			return suggestedTerm;
		}

		return "";
	}

	@Override
	public MatrixResults matrixSearch(Search searchRequest, List<String> fields, boolean countOnly, int width)
	{
		return indexer.matrixSearch(searchRequest, fields, countOnly);
	}
}
