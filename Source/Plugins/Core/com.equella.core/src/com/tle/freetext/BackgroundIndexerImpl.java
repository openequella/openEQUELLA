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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.BindFactory;
import com.tle.core.hibernate.CurrentDataSource;
import com.tle.core.hibernate.DataSourceHolder;
import com.tle.core.system.service.SchemaDataSourceService;
import com.tle.freetext.ItemSyncer.ItemSyncFactory;

public class BackgroundIndexerImpl implements BackgroundIndexer
{
	private static final int BATCH_PREPARE = 10;

	protected static final int MAX_WAITING = 10000;

	private static final Logger LOGGER = Logger.getLogger(BackgroundIndexerImpl.class);

	private static final int MAXTHREADS = 4;
	private static final int REAL_MAXTHREADS = MAXTHREADS * 2;
	private static final int MAXBATCH = 50;

	private static final long WAITTIME = TimeUnit.SECONDS.toMillis(2);
	private static final long CHECKTIME = TimeUnit.SECONDS.toMillis(15);
	private static final long INTERRUPT_MILLIS = TimeUnit.MINUTES.toMillis(2);

	@Inject
	private IndexedItemFactory indexedItemFactory;
	@Inject
	private ItemSyncFactory syncerFactory;
	@Inject
	private Provider<IndexerThread> indexerThreadProvider;
	@Inject
	private FreetextIndex freetextIndex;

	private Object listLock = new Object();
	private Object threadingLock = new Object();

	// Priority list of items waiting. Purposeful choice of LinkedList instead
	// of interface
	private LinkedList<IndexedItem> waitingList = new LinkedList<IndexedItem>(); // NOSONAR
	// Items needing to be indexed
	private Map<FullIdKey, IndexedItem> waitingMap = new HashMap<FullIdKey, IndexedItem>();
	// Items currently being indexed or waiting to be added to index
	private Map<FullIdKey, IndexedItem> indexingMap = new HashMap<FullIdKey, IndexedItem>();
	// Items finished indexing, but waiting to be added to lucene
	private LinkedList<IndexedItem> indexedList = new LinkedList<IndexedItem>(); // NOSONAR

	private final Stack<IndexerThread> freeThreads = new Stack<IndexerThread>();
	private final List<IndexerThread> inuseThreads = new LinkedList<IndexerThread>();

	private int indexerThreads;
	private long nextCheck;

	private int docsDone;
	private int docsDoneSinceLast;
	private int errors;
	private int deletions;
	private boolean dead;
	private volatile boolean wakeup;

	private final DataSourceHolder dataSource;
	private final long schemaId;
	private final SchemaDataSourceService schemaService;

	private ExecutorService syncFullExecutor = Executors.newCachedThreadPool();

	@AssistedInject
	public BackgroundIndexerImpl(@Assisted long schemaId, SchemaDataSourceService schemaService)
	{
		this.schemaId = schemaId;
		this.schemaService = schemaService;
		dataSource = schemaService.getDataSourceForId(schemaId);
	}

	@Override
	public void run()
	{
		CurrentDataSource.set(dataSource);
		while( true )
		{
			try
			{
				if( dead )
				{
					LOGGER.debug("dead, returning"); //$NON-NLS-1$
					return;
				}

				wakeup = false;
				boolean wait = processEvent();
				if( wait && !wakeup )
				{
					try
					{
						synchronized( this )
						{
							if( !wakeup )
							{
								wait(WAITTIME);
							}
						}
					}
					catch( InterruptedException e )
					{
						LOGGER.error("Interrupted"); //$NON-NLS-1$
					}
				}
			}
			catch( Throwable t )
			{
				LOGGER.error("Unhandled exception during event loop", t); //$NON-NLS-1$
			}
		}
	}

	public boolean processEvent()
	{
		boolean wait = true;
		long now = System.currentTimeMillis();
		boolean needsCheck = now >= nextCheck;

		wait &= checkNewIndexThreads(now);
		wait &= checkBatchIndex(now);
		wait &= checkSlowIndexes(now);

		if( needsCheck )
		{
			nextCheck = now + CHECKTIME;
			statusUpdate(now);
		}
		return wait;
	}

	@Override
	public void synchronizeFull(final Collection<Institution> institutions)
	{
		schemaService.executeWithSchema(syncFullExecutor, schemaId, syncerFactory.create(institutions, null, this));
	}

	@Override
	public void synchronizeNew(final Collection<Institution> institutions, final Date since)
	{
		schemaService.executeWithSchema(schemaId, syncerFactory.create(institutions, since, this));
	}

	@SuppressWarnings("nls")
	private boolean checkSlowIndexes(long now)
	{
		synchronized( threadingLock )
		{
			for( IndexerThread thread : inuseThreads )
			{
				IndexedItem indexedItem = thread.getIndexedItem();
				if( indexedItem != null )
				{
					if( !indexedItem.isOnIndexList() && !indexedItem.isNoLongerCurrent()
						&& !indexedItem.isFinishedAllIndexing() && !indexedItem.isIndexed()
						&& now >= indexedItem.getExpectedReturnTime() )
					{
						ItemIdKey itemKey = indexedItem.getItemIdKey();
						if( indexedItem.isFinishedFastIndexing() )
						{
							synchronized( listLock )
							{
								if( !indexedItem.isOnIndexList() )
								{
									LOGGER.info("Item overdue: " + itemKey + " indexing now.");
									addToIndexedList(indexedItem);
								}
							}
						}
						else
						{
							LOGGER.info("Item overdue: " + itemKey + " but still not \"fast\" indexed.");
						}
					}
				}
			}
			return true;
		}
	}

	@SuppressWarnings("nls")
	private boolean checkBatchIndex(long now)
	{
		List<IndexedItem> batch = null;
		synchronized( listLock )
		{
			if( indexedList.isEmpty() )
			{
				return true;
			}
			boolean overdue = now >= indexedList.element().getExpectedReturnTime();
			if( indexedList.size() >= MAXBATCH || indexerThreads == 0 || overdue )
			{
				int num = 0;
				if( overdue )
				{
					for( IndexedItem indItem : indexedList )
					{
						if( now < indItem.getExpectedReturnTime() )
						{
							break;
						}
						num++;
					}
				}
				else
				{
					num = Math.min(MAXBATCH, indexedList.size());
				}

				List<IndexedItem> indexBatch = indexedList.subList(0, num);
				for( IndexedItem item : indexBatch )
				{
					item.setOnIndexList(false);
				}
				batch = Lists.newArrayList(indexBatch);
				indexBatch.clear();
			}
			else
			{
				return true;
			}
		}

		try
		{
			LOGGER.info("Indexing batch of " + batch.size() + " items");
			freetextIndex.indexBatch(batch);
		}
		catch( Exception e )
		{
			LOGGER.error("Serious Error indexing a batch", e);
		}
		synchronized( listLock )
		{
			for( IndexedItem indexedItem : batch )
			{
				if( indexedItem.isFinishedAllIndexing() )
				{
					indexingMap.remove(indexedItem.getId());
				}
				indexedItem.setIndexed(true);
			}
			return false;
		}

	}

	private boolean checkNewIndexThreads(long now)
	{
		IndexedItem item = null;
		List<IndexedItem> batch = null;
		synchronized( listLock )
		{
			if( !waitingList.isEmpty() && indexerThreads < REAL_MAXTHREADS
				&& (indexerThreads < MAXTHREADS || now >= waitingList.element().getExpectedReturnTime()) )
			{
				item = waitingList.get(0);
				if( !item.isPrepared() )
				{
					int batchSize = BATCH_PREPARE;
					if( waitingList.size() < batchSize )
					{
						batchSize = waitingList.size();
					}
					batch = Lists.newArrayList(waitingList.subList(0, batchSize));
				}
			}
			else
			{
				return true;
			}
		}

		if( !Check.isEmpty(batch) )
		{
			populateItemBatch(batch);
		}

		synchronized( listLock )
		{
			if( item.isNoLongerCurrent() )
			{
				return false;
			}
			waitingList.remove(item);
			FullIdKey idKey = item.getId();
			waitingMap.remove(idKey);
			if( item.isAdd() )
			{
				indexingMap.put(idKey, item);

				if( item.isErrored() )
				{
					addErroredDoc(item);
				}
				else
				{
					IndexerThread indexerThread = getIndexerThread();
					indexerThread.startIndexing(item);
				}
			}
			else
			{
				indexingMap.put(idKey, item);
				item.setFinishedAllIndexing(true);
				addToIndexedList(item);
			}
			return false;
		}
	}

	@SuppressWarnings("nls")
	private void populateItemBatch(List<IndexedItem> batch)
	{
		try
		{
			freetextIndex.prepareItemsForIndexing(batch);
		}
		catch( Exception t )
		{
			LOGGER.error("Failed to prepare batch for indexing, trying 1", t);
			freetextIndex.prepareItemsForIndexing(batch.subList(0, 1));
		}

	}

	@SuppressWarnings("nls")
	private void statusUpdate(long now)
	{
		if( docsDoneSinceLast != docsDone || indexerThreads > 0 )
		{
			docsDoneSinceLast = docsDone;
			LOGGER.info("Report: " + indexerThreads + " threads running, " + waitingList.size() + " documents waiting, "
				+ docsDone + " done overall, " + errors + " errors, " + deletions
				+ " docs in Index but not in database");
			synchronized( threadingLock )
			{
				Iterator<IndexerThread> iter = inuseThreads.iterator();
				while( iter.hasNext() )
				{
					IndexerThread thread = iter.next();
					long millis = now - thread.getStarted();
					if( !thread.isAlive() )
					{
						LOGGER.error("Thread died:" + thread + " was indexing " + thread.getItemId());
						iter.remove();
						indexerThreads--;
					}
					else
					{
						if( millis > INTERRUPT_MILLIS )
						{
							Exception exception = new Exception();
							exception.setStackTrace(thread.getStackTrace());
							LOGGER.warn("Thread taking too long:" + thread, exception);
						}
						LOGGER.info("Thread " + thread + " indexing " + thread.getItemId() + " and has been for "
							+ millis + " milliseconds");
					}
				}
			}
		}
	}

	private IndexerThread getIndexerThread()
	{
		synchronized( threadingLock )
		{
			indexerThreads++;
			IndexerThread thread;
			if( freeThreads.size() == 0 )
			{
				thread = indexerThreadProvider.get();
				thread.setNumber(inuseThreads.size());
				thread.setBackground(this);
				thread.setExtensions(freetextIndex.getIndexingExtensions());
				thread.start();
			}
			else
			{
				thread = freeThreads.pop();
			}
			LOGGER.debug("**new inuse thread added**"); //$NON-NLS-1$
			inuseThreads.add(thread);
			return thread;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.freetext.BackgroundIndexer#addToQueue(com.tle.freetext.IndexedItem
	 * )
	 */
	@Override
	public void addToQueue(IndexedItem item)
	{
		addAllToQueue(Collections.singleton(item));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.freetext.BackgroundIndexer#addToQueue(com.tle.beans.item.ItemIdKey
	 * )
	 */
	@Override
	public void addToQueue(ItemIdKey key, boolean newSearcher)
	{
		IndexedItem item = indexedItemFactory.create(key, CurrentInstitution.get());
		item.setAdd(true);
		item.setNewSearcherRequired(newSearcher);
		addToQueue(item);
	}

	@Override
	public boolean isRoomForItems(int size)
	{
		return (waitingList.size() + indexingMap.size() + size) < MAX_WAITING;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.freetext.BackgroundIndexer#addAllToQueue(java.util.Collection)
	 */
	@Override
	public void addAllToQueue(Collection<IndexedItem> items)
	{
		synchronized( listLock )
		{
			for( IndexedItem item : items )
			{
				FullIdKey id = item.getId();
				IndexedItem existing = waitingMap.get(id);
				if( existing != null )
				{
					existing.setNoLongerCurrent(true);
					waitingList.remove(existing);
					waitingMap.remove(id);
				}
				existing = indexingMap.get(id);
				if( existing != null )
				{
					LOGGER.debug("Item " + item.getItemIdKey().toString() + " already on indexingMap. Removing.");
					existing.setNoLongerCurrent(true);
					indexingMap.remove(id);
					if( existing.isOnIndexList() )
					{
						indexedList.remove(existing);
					}
				}

				if( item.isAdd() )
				{
					priorityInsert(waitingList, item);
					waitingMap.put(id, item);
				}
				else
				{
					addToIndexedList(item);
					item.setFinishedAllIndexing(true);
					indexingMap.put(id, item);
				}
			}
		}
		wakeup();
	}

	private synchronized void wakeup()
	{
		wakeup = true;
		notifyAll();
	}

	private void addToIndexedList(IndexedItem item)
	{
		item.setOnIndexList(true);
		priorityInsert(indexedList, item);
	}

	private void priorityInsert(LinkedList<IndexedItem> list, IndexedItem item)
	{
		long retTime = item.getExpectedReturnTime();
		int i;
		for( i = 0; i < list.size(); i++ )
		{
			if( retTime <= list.get(i).getExpectedReturnTime() )
			{
				break;
			}
		}
		list.add(i, item);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.freetext.BackgroundIndexer#addIndexedDoc(com.tle.freetext.IndexedItem
	 * )
	 */
	public void addIndexedDoc(IndexedItem item)
	{
		synchronized( listLock )
		{
			// Only add doc if we are the one.
			if( !item.isNoLongerCurrent() )
			{
				item.setFinishedAllIndexing(true);
				// Only add it to the indexedList if it
				// isn't already there from the "slow" thread
				// checker
				if( !item.isOnIndexList() )
				{
					addToIndexedList(item);
				}
			}
		}
		wakeup();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.freetext.BackgroundIndexer#addErroredDoc(com.tle.freetext.IndexedItem
	 * )
	 */
	public void addErroredDoc(IndexedItem item)
	{
		synchronized( listLock )
		{
			FullIdKey id = item.getId();
			LOGGER.error("Error in doc " + id); //$NON-NLS-1$
			if( !item.isNoLongerCurrent() )
			{
				indexingMap.remove(id);
			}
			errors++;
		}
		wakeup();
	}

	public void threadFinished(IndexerThread thread)
	{
		synchronized( threadingLock )
		{
			indexerThreads--;
			docsDone++;
			LOGGER.debug("**inuse thread removed**"); //$NON-NLS-1$
			inuseThreads.remove(thread);
			freeThreads.push(thread);
		}
		wakeup();
	}

	public boolean isDead()
	{
		return dead;
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.freetext.BackgroundIndexer#kill()
	 */
	@Override
	@SuppressWarnings("nls")
	public void kill()
	{
		LOGGER.debug("Kill");

		synchronized( this )
		{
			LOGGER.debug("dead=true");
			dead = true;
		}

		for( IndexerThread t : freeThreads )
		{
			LOGGER.debug("freeThread.wakeup");
			t.wakeup();
		}

		for( IndexerThread t : inuseThreads )
		{
			LOGGER.debug("inuseThread.wakeup");
			t.wakeup();
		}
	}

	@Override
	public IndexedItem getIndexedItem(ItemIdKey key)
	{
		synchronized( listLock )
		{
			FullIdKey fullId = new FullIdKey(key.getKey(), CurrentInstitution.get().getUniqueId());
			IndexedItem indexedItem = waitingMap.get(fullId);
			if( indexedItem != null )
			{
				return indexedItem;
			}
			return indexingMap.get(fullId);
		}
	}

	@Override
	public IndexedItem createIndexedItem(ItemIdKey key)
	{
		return indexedItemFactory.create(key, CurrentInstitution.get());
	}

	@BindFactory
	public interface BackgroundIndexerFactory
	{
		BackgroundIndexerImpl create(long schemaId);
	}
}
