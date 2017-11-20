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

package com.tle.core.item.standard.service.impl;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.DRMPage;
import com.dytech.edge.wizard.beans.WizardPage;
import com.google.common.collect.Sets;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.ReferencedURL;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.DisplayNode;
import com.tle.beans.entity.itemdef.DynamicMetadataRule;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.ItemMetadataRule;
import com.tle.beans.entity.itemdef.SearchDetails;
import com.tle.beans.entity.itemdef.Wizard;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.core.collection.extension.CollectionSaveExtension;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.events.services.EventService;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.freetext.event.ItemReindexEvent;
import com.tle.core.freetext.reindex.SchemaFilter;
import com.tle.core.guice.Bind;
import com.tle.core.item.event.ItemMovedCollectionEvent;
import com.tle.core.item.event.ItemOperationBatchEvent;
import com.tle.core.item.event.ItemOperationEvent;
import com.tle.core.item.event.UpdateReferencedUrlsEvent;
import com.tle.core.item.event.listener.ItemMovedCollectionEventListener;
import com.tle.core.item.event.listener.UpdateReferencedUrlsListener;
import com.tle.core.item.operations.BaseFilter;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.FilterFactory;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.item.standard.service.ItemStandardService;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.schema.extension.SchemaSaveExtension;
import com.tle.core.services.FileSystemService;
import com.tle.core.url.URLCheckerService;
import com.tle.core.url.URLCheckerService.URLCheckMode;
import com.tle.core.url.URLEvent;
import com.tle.core.url.URLEvent.URLEventType;
import com.tle.core.url.URLListener;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
@Bind(ItemStandardService.class)
@Singleton
public class ItemStandardServiceImpl
	implements
		ItemStandardService,
		UserChangeListener,
		UpdateReferencedUrlsListener,
		URLListener,
		SchemaSaveExtension,
		CollectionSaveExtension,
		ItemMovedCollectionEventListener
{
	@Inject
	private ItemService itemService;
	@Inject
	private FilterFactory filterFactory;
	@Inject
	private ItemOperationFactory itemOperationFactory;
	@Inject
	private URLCheckerService urlCheckerService;
	@Inject
	private EventService eventService;
	@Inject
	private ItemDefinitionService collectionService;
	@Inject
	private FileSystemService fileSystemService;

	@Override
	@Transactional
	public void delete(ItemId itemId, boolean purge, boolean waitForIndex, boolean purgeIfDeleted)
	{
		Item item = itemService.getUnsecure(itemId);
		if( item.getStatus() == ItemStatus.DELETED )
		{
			if( !purge && !purgeIfDeleted)
			{
				return;
			}
			itemService.operation(itemId, itemOperationFactory.purge(waitForIndex));
			return;
		}
		WorkflowOperation secondOp;
		if( purge )
		{
			secondOp = itemOperationFactory.purge(waitForIndex);
		}
		else
		{
			secondOp = itemOperationFactory.save();
		}
		itemService.operation(itemId, itemOperationFactory.delete(), secondOp);
	}

	@Override
	public void updateReferencedUrlsEvent(UpdateReferencedUrlsEvent event)
	{
		itemService.operation(event.getItemKey(), new WorkflowOperation[]{itemOperationFactory.updateReferencedUrls(),
				itemOperationFactory.reIndexIfRequired(),});
	}

	@Override
	public void urlEvent(URLEvent event)
	{
		// Notify users of URLs reaching the warning or disabled stage.
		URLEventType type = event.getType();
		if( type == URLEventType.URL_WARNING || type == URLEventType.URL_DISABLED )
		{
			final ReferencedURL rurl = urlCheckerService.getUrlStatus(event.getUrl(), URLCheckMode.RECORDS_ONLY);
			itemService.operateAll(filterFactory.notifyBadUrl(rurl));
		}
	}

	@Override
	public void userDeletedEvent(UserDeletedEvent event)
	{
		itemService.operateAll(filterFactory.userDeleted(event.getUserID()), null);
	}

	@Override
	public void userEditedEvent(UserEditEvent event)
	{
		// Nothing to do here
	}

	@Override
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		itemService.operateAll(filterFactory.changeUserId(event.getFromUserId(), event.getToUserId()));
	}

	@Override
	public void schemaSaved(Schema oldSchema, Schema newSchema)
	{
		if( oldSchema != null )
		{
			// Refresh item name and description if either of the XPaths has changed
			if( !newSchema.getItemNamePath().equals(oldSchema.getItemNamePath())
				|| !newSchema.getItemDescriptionPath().equals(oldSchema.getItemDescriptionPath()) )
			{
				publishEventAfterCommit(new ItemOperationEvent(new FactoryMethodLocator<BaseFilter>(FilterFactory.class,
					"refreshSchemaItems", newSchema.getId())));
				return;
			}

			// Re-index items if the indexing settings for schema nodes has changed
			if( !getIndexedPaths(newSchema).equals(getIndexedPaths(oldSchema)) )
			{
				eventService.publishApplicationEvent(new ItemReindexEvent(new SchemaFilter(newSchema)));
				return;
			}
		}
	}

	@SuppressWarnings("nls")
	private Set<String> getIndexedPaths(Schema s)
	{
		Set<String> rv = new HashSet<String>();
		getIndexedPaths(rv, s.getDefinitionNonThreadSafe(), "");
		return rv;
	}

	@SuppressWarnings("nls")
	private void getIndexedPaths(Set<String> rv, PropBagEx xml, String path)
	{
		for( PropBagEx sxml : xml.iterator() )
		{
			final String spath = path + '/' + sxml.getNodeName();

			if( sxml.isNodeTrue("@field") )
			{
				rv.add("f" + spath);
			}

			if( sxml.isNodeTrue("@search") )
			{
				rv.add("s" + spath);
			}

			getIndexedPaths(rv, sxml, spath);
		}
	}

	private void publishEventAfterCommit(final ApplicationEvent<?> event)
	{
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter()
		{
			@Override
			public void afterCommit()
			{
				eventService.publishApplicationEvent(event);
			}
		});
	}

	@Override
	public void collectionSaved(ItemDefinition oldCollection, ItemDefinition newCollection)
	{
		processDRM(newCollection);

		if( oldCollection != null )
		{
			boolean fireEvent = false;
			final ItemOperationBatchEvent batchEvent = new ItemOperationBatchEvent();

			// Check if the workflow has changed
			if( !Objects.equals(oldCollection.getWorkflow(), newCollection.getWorkflow()) )
			{
				fireEvent = true;
				batchEvent.addEvent(new ItemOperationEvent(new FactoryMethodLocator<BaseFilter>(FilterFactory.class,
					"workflowChanged", newCollection.getId())));
			}

			if( metadataRuleChanged(oldCollection.getItemMetadataRules(), newCollection)
				|| dynamicMetadataRuleChanged(oldCollection.getDynamicMetadataRules(), newCollection)
				|| searchDisplayNodesChanged(oldCollection.getSearchDetails(), newCollection.getSearchDetails()) )
			{
				fireEvent = true;
				batchEvent.addEvent(new ItemOperationEvent(new FactoryMethodLocator<BaseFilter>(FilterFactory.class,
					"refreshCollectionItems", newCollection.getId())));
			}

			if( fireEvent )
			{
				publishEventAfterCommit(batchEvent);
			}
		}
	}

	@Transactional
	@Override
	public void itemMovedCollection(ItemMovedCollectionEvent event)
	{
		// Delete the old folder if the filestore ID of the old collection != filestore ID of new collection
		// OR they are the same, but one is buckets and the other is not
		final String oldCollectionUuid = event.getFromCollectionUuid();
		final String newCollectionUuid = event.getToCollectionUuid();
		if( !oldCollectionUuid.equals(newCollectionUuid) )
		{
			final ItemDefinition oldCollection = collectionService.getByUuid(oldCollectionUuid);
			final ItemDefinition newCollection = collectionService.getByUuid(newCollectionUuid);
			final String oldFilestoreId = oldCollection
				.getAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_FILESTORE);
			final String newFilestoreId = newCollection
				.getAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_FILESTORE);
			final boolean oldBucket = oldCollection.getAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_BUCKETS,
				false);
			final boolean newBucket = newCollection.getAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_BUCKETS,
				false);
			if( !Objects.equals(oldFilestoreId, newFilestoreId) || oldBucket != newBucket )
			{
				final ItemKey itemId = event.getItemId();
				ItemFile oldHandle = new ItemFile(itemId.getUuid(), itemId.getVersion(),
					oldBucket ? oldCollectionUuid : null);
				oldHandle.setFilestoreId("default".equals(oldFilestoreId) ? null : oldFilestoreId);
				fileSystemService.removeFile(oldHandle);
			}
		}
	}

	private void processDRM(ItemDefinition itemdef)
	{
		Set<String> pageIds = Sets.newHashSet();
		Wizard wizard = itemdef.getWizard();
		if( wizard != null )
		{
			List<WizardPage> pages = wizard.getPages();
			for( WizardPage page : pages )
			{
				if( page instanceof DRMPage )
				{
					pageIds.add(((DRMPage) page).getUuid());
				}
			}
			if( !pageIds.isEmpty() )
			{
				publishEventAfterCommit(new ItemOperationEvent(new FactoryMethodLocator<BaseFilter>(FilterFactory.class,
					"drmUpdate", itemdef.getId(), (Serializable) pageIds)));
			}
		}
	}

	private boolean metadataRuleChanged(List<ItemMetadataRule> oldRules, ItemDefinition newCollection)
	{
		List<ItemMetadataRule> newRules = newCollection.getItemMetadataRules();
		return !listContentsIdentical(oldRules, newRules, new Comparator<ItemMetadataRule>()
		{
			@Override
			public int compare(ItemMetadataRule o1, ItemMetadataRule o2)
			{
				// Id and Script. Don't care about name changes
				if( !Objects.equals(o1.getId(), o2.getId()) || !Objects.equals(o1.getScript(), o2.getScript()) )
				{
					return 1;
				}

				return 0;
			}
		});
	}

	private boolean dynamicMetadataRuleChanged(List<DynamicMetadataRule> oldRules, ItemDefinition newCollection)
	{
		List<DynamicMetadataRule> newRules = newCollection.getDynamicMetadataRules();
		return !listContentsIdentical(oldRules, newRules, new Comparator<DynamicMetadataRule>()
		{
			@Override
			public int compare(DynamicMetadataRule o1, DynamicMetadataRule o2)
			{
				// Id, Path, Type, Privileges. Don't care about name changes
				if( !Objects.equals(o1.getId(), o2.getId()) || !Objects.equals(o1.getPath(), o2.getPath())
					|| !Objects.equals(o1.getType(), o2.getType())
					|| !Check.bothNullOrDeepEqual(o1.getTargetList(), o2.getTargetList()) )
				{
					return 1;
				}

				return 0;
			}
		});
	}

	private boolean searchDisplayNodesChanged(SearchDetails oldDetails, SearchDetails newDetails)
	{
		List<DisplayNode> oldNodes = oldDetails == null ? null : oldDetails.getDisplayNodes();
		List<DisplayNode> newNodes = newDetails == null ? null : newDetails.getDisplayNodes();
		return !listContentsIdentical(oldNodes, newNodes, new Comparator<DisplayNode>()
		{
			@Override
			public int compare(DisplayNode node1, DisplayNode node2)
			{
				return Check.bothNullOrDeepEqual(node1, node2) ? 0 : 1;
			}
		});
	}

	private <T> boolean listContentsIdentical(List<T> oldRules, List<T> newRules, Comparator<T> comparator)
	{
		if( oldRules == null && newRules == null || (oldRules == null && newRules != null && newRules.size() == 0) )
		{
			return true;
		}
		else if( oldRules == null || newRules == null || oldRules.size() != newRules.size() )
		{
			return false;
		}
		else
		{
			Iterator<T> ai = oldRules.iterator();
			Iterator<T> bi = newRules.iterator();

			while( ai.hasNext() )
			{
				if( comparator.compare(ai.next(), bi.next()) != 0 )
				{
					return false;
				}
			}

			return true;
		}
	}
}
