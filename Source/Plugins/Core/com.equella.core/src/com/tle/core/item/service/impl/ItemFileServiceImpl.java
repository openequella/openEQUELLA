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

package com.tle.core.item.service.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.common.filesystem.remoting.RemoteFileSystemService;
import com.tle.common.util.Logger;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.event.ItemMovedCollectionEvent;
import com.tle.core.item.event.listener.ItemMovedCollectionEventListener;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.LoggingService;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
@Bind(ItemFileService.class)
@Singleton
public class ItemFileServiceImpl implements ItemFileService, ItemMovedCollectionEventListener
{
	private Logger logger;
	@Inject
	private ItemDefinitionService collectionService;
	@Inject
	private FileSystemService fileSystemService;
	private InstitutionCache<Cache<ItemId, CollectionStorage>> cache;

	@Inject
	public void setInstitutionService(InstitutionService service)
	{
		cache = service.newInstitutionAwareCache(new CacheLoader<Institution, Cache<ItemId, CollectionStorage>>()
		{
			@Override
			public Cache<ItemId, CollectionStorage> load(Institution key)
			{
				return CacheBuilder.newBuilder().maximumSize(10000).expireAfterAccess(30, TimeUnit.MINUTES).build();
			}
		});
	}

	@Inject
	public void setLoggingService(LoggingService loggingService)
	{
		logger = loggingService.getLogger(ItemFileService.class);
	}

	@Override
	public ItemFile getItemFile(String uuid, int version, @Nullable ItemDefinition collection)
	{
		return getItemFile(new ItemId(uuid, version), collection);
	}

	@Override
	public ItemFile getItemFile(ItemKey itemId, @Nullable ItemDefinition collection)
	{
		if( !fileSystemService.isAdvancedFilestore() )
		{
			return new ItemFile(itemId, null);
		}
		if( collection != null )
		{
			return resolveItemFile(itemId, collection);
		}

		//Sigh... we need to work out the collection
		final CollectionStorage collectionStorage = getCollectionStorage(itemId);
		return resolveItemFile(itemId, collectionStorage.getUuid(), collectionStorage.getFilestoreId());
	}

	@Override
	public ItemFile getItemFile(Item item)
	{
		return getItemFile(item.getItemId(), item.getItemDefinition());
	}

	@Override
	public void itemMovedCollection(ItemMovedCollectionEvent event)
	{
		cache.getCache().invalidate(ItemId.fromKey(event.getItemId()));
	}

	private ItemFile resolveItemFile(ItemKey itemId, ItemDefinition collection)
	{
		return resolveItemFile(itemId, getBucketFolder(collection), getFilestoreId(collection));
	}

	private ItemFile resolveItemFile(ItemKey itemId, @Nullable String bucketFile, @Nullable String filestoreId)
	{
		final ItemFile itemFile = new ItemFile(itemId, bucketFile);
		itemFile.setFilestoreId(filestoreId);
		return itemFile;
	}

	@Nullable
	protected String getBucketFolder(ItemDefinition collection)
	{
		if( collection.getAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_BUCKETS, false) )
		{
			return collection.getUuid();
		}
		return null;
	}

	@Nullable
	protected String getFilestoreId(ItemDefinition collection)
	{
		final String filestoreId = collection.getAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_FILESTORE);
		if( filestoreId != null && !filestoreId.equals(RemoteFileSystemService.DEFAULT_FILESTORE_ID) )
		{
			return filestoreId;
		}
		return null;
	}

	private CollectionStorage getCollectionStorage(final ItemKey itemId)
	{
		try
		{
			return cache.getCache().get(ItemId.fromKey(itemId), new FilestoreCacheLoader(itemId));
		}
		catch( ExecutionException ee )
		{
			throw Throwables.propagate(ee);
		}
	}

	public static class CollectionStorage
	{
		private String uuid;
		private String filestoreId;

		public String getUuid()
		{
			return uuid;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}

		public String getFilestoreId()
		{
			return filestoreId;
		}

		public void setFilestoreId(String filestoreId)
		{
			this.filestoreId = filestoreId;
		}
	}

	private class FilestoreCacheLoader implements Callable<CollectionStorage>
	{
		private final ItemKey itemId;

		public FilestoreCacheLoader(ItemKey itemId)
		{
			this.itemId = itemId;
		}

		@Override
		public CollectionStorage call() throws Exception
		{
			if( logger.isTraceEnabled() )
			{
				logger.trace("Cache miss for item ID " + itemId.toString());
			}
			CollectionStorage storage = new CollectionStorage();
			ItemDefinition collection = collectionService.getByItemIdUnsecure(itemId);
			storage.setUuid(getBucketFolder(collection));
			storage.setFilestoreId(getFilestoreId(collection));
			return storage;
		}
	}
}