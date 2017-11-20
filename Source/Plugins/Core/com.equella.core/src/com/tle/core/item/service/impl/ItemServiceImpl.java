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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.MDC;
import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria;
import org.java.plugin.registry.Extension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.dytech.edge.common.LockedException;
import com.dytech.edge.common.PropBagWrapper;
import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.exceptions.AttachmentNotFoundException;
import com.dytech.edge.exceptions.ItemNotFoundException;
import com.dytech.edge.exceptions.WorkflowException;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.DynamicMetadataRule;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.ItemMetadataRule;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemKeyExtension;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemSelect;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ItemXml;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.workflow.SecurityStatus;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.Triple;
import com.tle.common.collection.AttachmentConfigConstants;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.scripting.service.ScriptContextCreationParams;
import com.tle.common.scripting.service.ScriptingService;
import com.tle.common.security.PrivilegeTree;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.common.security.WorkflowTaskDynamicTarget;
import com.tle.common.settings.standard.AutoLogin;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.valuebean.DefaultUserBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.util.Logger;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.events.services.EventService;
import com.tle.core.guice.Bind;
import com.tle.core.item.ItemIdExtension;
import com.tle.core.item.dao.ItemDao;
import com.tle.core.item.event.ItemOperationBatchEvent;
import com.tle.core.item.event.ItemOperationEvent;
import com.tle.core.item.event.listener.ItemOperationBatchListener;
import com.tle.core.item.event.listener.ItemOperationListener;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.helper.ItemHelper.ItemHelperSettings;
import com.tle.core.item.operations.FilterResultListener;
import com.tle.core.item.operations.ItemOperationFilter;
import com.tle.core.item.operations.ItemOperationFilter.FilterResults;
import com.tle.core.item.operations.ItemOperationParams;
import com.tle.core.item.operations.ItemOperationParamsImpl;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.scripting.WorkflowScriptConstants;
import com.tle.core.item.scripting.WorkflowScriptContextParams;
import com.tle.core.item.scripting.WorkflowScriptObjectContributor;
import com.tle.core.item.service.ItemLockingService;
import com.tle.core.item.service.ItemResolverExtension;
import com.tle.core.item.service.ItemService;
import com.tle.core.notification.NotificationService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.TLEAclManager;
import com.tle.core.security.impl.ItemDynamicMetadataTarget;
import com.tle.core.security.impl.SecureAllOnCall;
import com.tle.core.security.impl.SecureInModeration;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureNotInModeration;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.services.LoggingService;
import com.tle.core.services.user.UserService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.AccessDeniedException;

/*
 * @author Nicholas Read
 */
@NonNullByDefault
@Bind(ItemService.class)
@Singleton
public class ItemServiceImpl
	implements
		ItemService,
		ItemResolverExtension,
		ItemOperationListener,
		ItemOperationBatchListener
{
	private static final String ITEM_METADATA_SECURITY_RULE_SCRIPT_NAME = "saveSecurityMetdataRule";

	private final Cache<String, Set<String>> privilegeCache = CacheBuilder.newBuilder().softValues()
		.expireAfterAccess(30, TimeUnit.SECONDS).build();
	private final Map<String, List<Extension>> operationExtensionMap = new HashMap<String, List<Extension>>();
	private final Collection<String> itemPrivileges;
	private final Random mdcNums = new Random();
	private Logger logger;

	@Inject
	private ItemDao dao;
	@Inject
	private ItemLockingService lockingService;
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private AuditLogService auditLogService;
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private EventService eventService;
	@Inject
	private ScriptingService scriptingService;
	@Inject
	private UserService userService;
	@Inject
	private NotificationService notificationService;
	@Inject
	private ConfigurationService configurationService;

	// plugin extensions
	@Inject
	private PluginTracker<WorkflowOperation> operationTracker;
	@Inject
	private PluginTracker<WorkflowScriptObjectContributor> scriptObjectTracker;
	@Inject
	private PluginTracker<ItemIdExtension> idTracker;

	@Inject
	public void setLoggingService(LoggingService loggingService)
	{
		logger = loggingService.getLogger(ItemService.class);
	}

	public ItemServiceImpl()
	{
		itemPrivileges = new HashSet<String>();
		itemPrivileges.addAll(PrivilegeTree.getPrivilegesForNode(Node.WORKFLOW_TASK));
		itemPrivileges.addAll(PrivilegeTree.getPrivilegesForNode(Node.ITEM));
		itemPrivileges.add(SecurityConstants.CREATE_ITEM);
	}

	@Override
	public List<ItemId> getItemsWithUrl(String url, ItemDefinition itemDefinition, String excludedUuid)
	{
		Check.checkNotEmpty(url);
		return dao.getItemsWithUrl(url, itemDefinition, excludedUuid);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.item.ItemService#getVersionDetails(java.lang.String
	 * )
	 */
	@Override
	public List<Item> getVersionDetails(String uuid)
	{
		return dao.getAllVersionsOfItem(uuid);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.item.ItemService#getNextLiveItems(com.dytech.edge
	 * .common.valuebean.ItemKey[])
	 */
	@Override
	@Transactional
	public List<Item> getNextLiveItems(List<ItemId> items)
	{
		return dao.getNextLiveItems(items);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.item.ItemService#getLatestVersion(com.dytech.edge
	 * .common.valuebean.ItemKey)
	 */
	@Override
	@Transactional
	public int getLatestVersion(String uuid)
	{
		return dao.getLatestVersion(uuid);
	}

	@Override
	@Transactional
	public Item getLatestVersionOfItem(String uuid)
	{
		return dao.getAllVersionsOfItem(uuid).get(0);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.item.ItemService#getLiveItemVersion(java.lang.String
	 * )
	 */
	@Override
	@Transactional
	public int getLiveItemVersion(String uuid)
	{
		return dao.getLatestLiveVersion(uuid);
	}

	@Override
	@Transactional
	public ItemIdKey getLiveItemVersionId(String uuid)
	{
		return dao.getLatestLiveVersionId(uuid);
	}

	@SuppressWarnings("nls")
	private ItemKey ensureItemKey(final ItemKey key)
	{
		ItemKey result = key;

		int ver = key.getVersion();
		if( ver == 0 )
		{
			throw new Error("Real version numbers are used in ItemService");
		}

		String uuid = key.getUuid();
		if( uuid.length() == 0 )
		{
			throw new Error("Should have a UUID already");
		}
		return result;
	}

	@Override
	public Item getUnsecure(ItemKey itemId)
	{
		return getInternal(itemId, false);
	}

	@Override
	public Item getUnsecureIfExists(ItemKey itemId)
	{
		itemId = ensureItemKey(itemId);
		return dao.get(itemId);
	}

	@Override
	@SecureOnReturn(priv = AttachmentConfigConstants.VIEW_ATTACHMENTS)
	@Transactional(propagation = Propagation.REQUIRED, noRollbackFor = ItemNotFoundException.class)
	public Item getItemWithViewAttachmentPriv(ItemKey key)
	{
		return getInternal(key, false);
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.VIEW_ITEM)
	@Transactional(propagation = Propagation.REQUIRED, noRollbackFor = ItemNotFoundException.class)
	public Item get(ItemKey key)
	{
		return getInternal(key, false);
	}

	@Transactional(propagation = Propagation.REQUIRED, noRollbackFor = ItemNotFoundException.class, readOnly = true)
	protected Item getUnsecureReadOnly(ItemKey key)
	{
		return getInternal(key, true);
	}

	private Item getInternal(ItemKey key, boolean readOnly)
	{
		key = ensureItemKey(key);
		Item item = dao.get(key, readOnly);
		if( item == null )
		{
			String message = CurrentLocale.get("com.tle.core.services.item.error.itemnotfound", key);
			logger.warn(message);

			throw new ItemNotFoundException(key);
		}
		return item;
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.EDIT_ITEM)
	@Transactional
	public Item getForEdit(ItemKey key)
	{
		return dao.getExistingItem(key);
	}

	@Override
	@SecureOnReturn(priv = "NEWVERSION_ITEM")
	@Transactional
	public Item getForNewVersion(ItemKey itemId)
	{
		return dao.getExistingItem(itemId);
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.VIEW_ITEM)
	public String getAsXml(ItemId key)
	{
		ItemPack pack = getItemPack(key);
		return itemHelper.convertToXml(pack, new ItemHelperSettings(true)).toString();
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.VIEW_ITEM)
	public ItemPack getItemPack(ItemKey key)
	{
		ItemPack pack = new ItemPack();
		Item item = getInternal(key, false);
		pack.setItem(item);
		pack.setXml(getItemXmlPropBag(item));
		return pack;
	}

	@Override
	public PropBagEx getItemXmlPropBag(Item item)
	{
		ItemXml itemXml = item.getItemXml();
		return (itemXml == null ? new PropBagEx() : new PropBagEx(itemXml.getXml()));
	}

	@Override
	public PropBagEx getItemXmlPropBag(ItemKey key)
	{
		return getItemXmlPropBag(getInternal(key, true));
	}

	@Override
	public List<Item> queryItems(List<Long> itemkeys)
	{
		return dao.getItems(itemkeys, new ItemSelect(), CurrentInstitution.get());
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.services.ItemService#queryItems(java.util.List)
	 */
	@Override
	public List<Item> queryItems(List<ItemIdKey> itemkeys, ItemSelect select)
	{
		List<Long> longs = new ArrayList<Long>(itemkeys.size());
		for( ItemIdKey id : itemkeys )
		{
			longs.add(id.getKey());
		}
		return dao.getItems(longs, select, CurrentInstitution.get());
	}

	@Override
	public <A> List<A> queryItems(DetachedCriteria criteria, Integer firstResult, Integer maxResults)
	{
		return dao.findAnyByCriteria(criteria, firstResult, maxResults);
	}

	@Override
	public List<Item> queryItemsByUuids(List<String> uuids)
	{
		return dao.getItems(uuids, CurrentInstitution.get());
	}

	@Override
	public Map<ItemId, Item> queryItemsByItemIds(List<? extends ItemKey> itemIds)
	{
		return dao.getItems(itemIds);
	}

	@Override
	public void updateIndexTimes(String whereClause, String[] names, Object[] values)
	{
		int count = dao.updateIndexTimes(whereClause, names, values);
		logger.info("Updated " + count + " items"); //$NON-NLS-2$
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public List<ItemId> enumerateItems(String whereClause, String[] names, Object[] values)
	{
		return dao.enumerateItemKeys(whereClause, names, values);
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public List<ItemIdKey> getItemIdKeys(List<Long> ids)
	{
		return dao.getItemIdKeys(ids);
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public ItemIdKey getItemIdKey(Long id)
	{
		return dao.getItemIdKey(id);
	}

	@Override
	public List<Triple<String, Integer, String>> enumerateItemNames(String whereClause, String[] names, Object[] values)
	{
		return dao.enumerateItemNames(whereClause, names, values);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.item.ItemService#forceUnlock(com.tle.beans.item
	 * .Item)
	 */
	@Override
	public void forceUnlock(Item item)
	{
		try
		{
			lockingService.unlock(item, true);
		}
		catch( LockedException e )
		{
			logger.warn(CurrentLocale.get("com.tle.core.services.item.error.stilllockedexception"), e);
		}
	}

	@Override
	public String getNameForId(long id)
	{
		return dao.getNameForId(id);
	}

	@Override
	public Map<ItemId, LanguageBundle> getItemNames(Collection<? extends ItemKey> keys)
	{
		return dao.getItemNames(keys);
	}

	@Override
	public Map<ItemId, Long> getItemNameIds(Collection<? extends ItemKey> keys)
	{
		return dao.getItemNameIds(keys);
	}

	@Override
	public Set<String> getCachedPrivileges(ItemKey itemKey)
	{
		return privilegeCache.getIfPresent(getPrivCacheId(itemKey));
	}

	@Override
	public Set<String> getReferencedUsers()
	{
		return dao.getReferencedUsers();
	}

	@Override
	public void delete(Item item)
	{
		auditLogService.logItemPurged(item);
		dao.delete(item);
	}

	@Override
	@Transactional(noRollbackFor = AttachmentNotFoundException.class)
	public Attachment getAttachmentForUuid(ItemKey itemId, String uuid)
	{
		Attachment attachment = dao.getAttachmentByUuid(itemId, uuid);
		if( attachment == null )
		{
			throw new AttachmentNotFoundException(itemId, uuid);
		}
		return attachment;
	}

	@Override
	public Map<String, Object> getItemInfo(ItemId id)
	{
		return dao.getItemInfo(id.getUuid(), id.getVersion());
	}

	@Override
	public Set<String> unionItemUuidsWithCollectionUuids(Collection<String> itemUuids, Set<String> collectionIds)
	{
		return dao.unionItemUuidsWithCollectionUuids(itemUuids, collectionIds);
	}

	private List<WorkflowOperation> getOperationListByType(String type)
	{
		List<Extension> extensions = getExtensionListByType(type);
		List<WorkflowOperation> ops = new ArrayList<WorkflowOperation>();
		if( extensions != null )
		{
			for( Extension extension : extensions )
			{
				WorkflowOperation op = operationTracker.getBeanByParameter(extension, "class");
				ops.add(op);
			}
		}
		return ops;
	}

	private synchronized List<Extension> getExtensionListByType(String type)
	{
		if( operationTracker.needsUpdate() )
		{
			operationExtensionMap.clear();
			List<Extension> allExtension = operationTracker.getExtensions();
			for( Extension extension : allExtension )
			{
				String extType = extension.getParameter("type").valueAsString();
				List<Extension> extList = operationExtensionMap.get(extType);
				if( extList == null )
				{
					extList = new ArrayList<Extension>();
					operationExtensionMap.put(extType, extList);
				}
				extList.add(extension);
			}
		}
		return operationExtensionMap.get(type);
	}

	@Override
	@Transactional
	public Multimap<Item, Attachment> getAttachmentsForItems(Collection<Item> items)
	{
		return dao.getAttachmentsForItems(items);
	}

	@Override
	@Transactional
	public Attachment getAttachmentForFilepath(ItemKey itemId, String filepath)
	{
		return dao.getAttachmentByFilepath(itemId, filepath);
	}

	@Override
	public List<String> getNavReferencedAttachmentUuids(List<Item> items)
	{
		return dao.getNavReferencedAttachmentUuids(items);
	}

	@Override
	public <I extends IItem<?>> I resolveItem(ItemKey itemKey)
	{
		return (I) getUnsecure(itemKey);
	}

	@Override
	public PropBagEx resolveXml(IItem<?> item)
	{
		return getItemXmlPropBag((Item) item);
	}

	@Override
	public IAttachment resolveAttachment(ItemKey itemKey, String attachmentUuid)
	{
		return getAttachmentForUuid(itemKey, attachmentUuid);
	}

	@Override
	public boolean checkRestrictedAttachment(IItem<?> item, IAttachment attachment)
	{
		final boolean restricted = attachment.isRestricted();
		if( !restricted )
		{
			return false;
		}
		return canViewRestrictedAttachments(item);
	}

	@Override
	public boolean canViewRestrictedAttachments(IItem<?> item)
	{
		return aclManager.filterNonGrantedPrivileges(item, AttachmentConfigConstants.VIEW_RESTRICTED_ATTACHMENTS)
			.isEmpty();
	}

	@Override
	public boolean canRestrictAttachments(IItem<?> item)
	{
		return !aclManager.filterNonGrantedPrivileges(item, AttachmentConfigConstants.RESTRICT_ATTACHMENTS).isEmpty();
	}

	/*
	 * Operations
	 */

	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public void operateAll(ItemOperationFilter filter)
	{
		operateAllPrivate(filter, null);
	}

	@Override
	@Transactional
	public void operateAllInTransaction(ItemOperationFilter filter)
	{
		operateAllPrivate(filter, null);
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public void operateAll(final ItemOperationFilter filter, final FilterResultListener listener)
	{
		operateAllPrivate(filter, listener);
	}

	private ItemOperationParams operateAllPrivate(final ItemOperationFilter filter, final FilterResultListener listener)
	{
		final ItemOperationParams params = new ItemOperationParamsImpl();
		final boolean containsDelete = containsDelete(filter.getOperations());

		filter.setDateNow(params.getDateNow());

		FilterResults itemIds = filter.getItemIds();

		if( listener != null )
		{
			listener.total((int) itemIds.getTotal());
		}

		Iterator<? extends ItemKey> iter = itemIds.getResults();
		while( iter.hasNext() )
		{
			ItemKey itemKey = iter.next();
			processItemId(filter, listener, params, containsDelete, itemKey);
		}

		postProcessParameters(params);
		logger.info("Finished operateAll on: " + filter.getClass());
		return params;
	}

	private void processItemId(final ItemOperationFilter filter, final FilterResultListener listener,
		final ItemOperationParams params, final boolean containsDelete, final ItemKey itemId)
	{
		Item original = null;
		if( containsDelete )
		{
			original = reloadItem(itemId);
		}

		try
		{
			logger.debug("Processing " + itemId + " with filter " + filter.getClass().getName());
			ItemPack result = runOperation(itemId, params, filter.getOperations());
			if( result != null )
			{
				result.setItem(reloadItem(itemId));
			}
			else
			{
				result = new ItemPack(original, null, null);
			}
			if( listener != null )
			{
				listener.succeeded(itemId, result);
			}
		}
		catch( Exception e )
		{
			// if original is null, see if we can get it so
			// we can display the name to the user.
			// if not then no big deal.
			try
			{
				if( original == null )
				{
					original = reloadItem(itemId);
				}
			}
			catch( Exception t )
			{
				// don't care
			}
			if( listener != null )
			{
				listener.failed(itemId, original, params.getItemPack(), Throwables.getRootCause(e));
			}
		}
		finally
		{
			dao.clear();
		}
	}

	protected boolean containsDelete(WorkflowOperation[] ops)
	{
		for( WorkflowOperation op : ops )
		{
			if( op.isDeleteLike() )
			{
				return true;
			}
		}
		return false;
	}

	@Transactional
	protected Item reloadItem(ItemKey id)
	{
		Item item = dao.get(id);
		if( item != null )
		{
			Hibernate.initialize(item);
			initBundle(item.getName());
		}
		return item;
	}

	@Transactional
	protected ItemPack<Item> runOperation(ItemKey key, ItemOperationParams params, WorkflowOperation... operations)
	{
		try
		{
			MDC.put(Constants.MDC_ITEM_ID, Integer.toString(mdcNums.nextInt() & 0xfff));

			ItemPack<Item> pack = null;
			long itemId = 0;
			if( key != null )
			{
				boolean readonly = true;
				for( WorkflowOperation operation : operations )
				{
					readonly &= operation.isReadOnly();
				}
				key = ensureItemKey(key);
				Item item = dao.get(key, readonly);
				// I would think item==null is an error??
				if( item != null )
				{
					itemId = item.getId();
					if( logger.isDebugEnabled() )
					{
						logger.debug("Running operations on " + itemId);
						logger.debug(operations.toString());
					}
					PropBagEx xml = getItemXmlPropBag(item);
					if( key instanceof ItemKeyExtension )
					{
						String extensionId = ((ItemKeyExtension) key).getExtensionId();
						idTracker.getBeanMap().get(extensionId).setup(key, params, item);
					}
					pack = new ItemPack<Item>();
					pack.setItem(item);
					pack.setXml(xml);

					// dao.unlinkFromSession(pack.getItem());
					params.setUpdate(true);
				}
			}
			params.reset(key, itemId, pack);

			SecurityStatus securityStatus = new SecurityStatus();
			execute(operations, params, securityStatus);
			perItemPostProcess(params);
		}
		catch( WorkflowException we )
		{
			logger.error("Error operating on item: " + key + " : " + we.getLocalizedMessage());
			throw we;
		}
		catch( Exception t )
		{
			logger.error("Error performing workflow on item: " + key, t);
			if( t instanceof RuntimeException )
			{
				throw ((RuntimeException) t);
			}
			else
			{
				throw new WorkflowException(t);
			}
		}
		finally
		{
			MDC.remove(Constants.MDC_ITEM_ID);
			dao.flush();
		}
		return params.getItemPack();
	}

	protected void initBundle(LanguageBundle bundle)
	{
		if( bundle != null )
		{
			Hibernate.initialize(bundle.getStrings());
		}
	}

	protected void perItemPostProcess(final ItemOperationParams params)
	{
		final List<ApplicationEvent<?>> eventList = new ArrayList<ApplicationEvent<?>>(params.getAfterCommitEvents());
		final List<Runnable> afterCommitHooks = new ArrayList<Runnable>(params.getAfterCommit());
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter()
		{
			@Override
			public void afterCommit()
			{
				afterCommitOne(eventList, afterCommitHooks);
			}
		});
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	protected void afterCommitOne(List<ApplicationEvent<?>> eventList, List<Runnable> afterCommitHooks)
	{
		for( Runnable runnable : afterCommitHooks )
		{
			runnable.run();
		}
		for( ApplicationEvent<?> event : eventList )
		{
			eventService.publishApplicationEvent(event);
		}
	}

	private void postProcessParameters(final ItemOperationParams params)
	{
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter()
		{
			@Override
			public void afterCommit()
			{
				afterCommitAll(params);
			}
		});
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	protected void afterCommitAll(ItemOperationParams params)
	{
		if( params.isNotificationsAdded() )
		{
			notificationService.processEmails();
		}
		for( ApplicationEvent<?> event : params.getAfterCommitAllEvents() )
		{
			eventService.publishApplicationEvent(event);
		}

		List<ItemOperationFilter> filters = params.getExtraFilters();
		if( filters != null )
		{
			for( ItemOperationFilter filter : filters )
			{
				try
				{
					operateAll(filter);
				}
				catch( WorkflowException e )
				{
					logger.error("Error running filter", e);
				}
			}
		}
	}

	@Override
	@Transactional(propagation = Propagation.NEVER)
	public void itemOperationEvent(ItemOperationEvent event)
	{
		try
		{
			operateAll(event.getOperation(), null);
		}
		catch( WorkflowException ex )
		{
			logger.error(CurrentLocale.get("com.tle.core.services.item.error.operation"), ex);
		}
	}

	@Override
	public void itemOperationBatchEvent(ItemOperationBatchEvent event)
	{
		for( ItemOperationEvent subEvent : event.getEvents() )
		{
			itemOperationEvent(subEvent);
		}
	}

	private void execute(WorkflowOperation[] operations, ItemOperationParams params, SecurityStatus securityStatus)
	{

		for( WorkflowOperation operation : operations )
		{
			params.setModified(execute(operation, params, securityStatus) | params.isModified());
			List<WorkflowOperation> ops = params.getOperations();
			if( ops != null )
			{
				WorkflowOperation[] ops2 = ops.toArray(new WorkflowOperation[ops.size()]);
				ops.clear();
				execute(ops2, params, securityStatus);
			}
		}
	}

	private boolean execute(WorkflowOperation op, ItemOperationParams params, SecurityStatus status)
	{
		if( op.failedToAutowire() )
		{
			throw new Error("Code error " + op.getClass() + " was not injected");
		}
		if( params.isUpdateSecurity() )
		{
			ItemPack<Item> itemPack = params.getItemPack();

			if( itemPack != null )
			{
				Item item = itemPack.getItem();
				Set<String> privs = new HashSet<String>();
				if( !item.isNewItem() )
				{
					status.setLock(lockingService.get(item));
				}
				privs.addAll(aclManager.filterNonGrantedPrivileges(params.getSecurityObject(), itemPrivileges));

				String id = getPrivCacheId(params.getItemKey());
				privilegeCache.put(id, privs);
				status.setAllowedPrivileges(privs);
			}
			else
			{
				status.setLock(null);
				status.setAllowedPrivileges(ImmutableSet.of(SecurityConstants.CREATE_ITEM));
			}
			params.setUpdateSecurity(false);
			params.setSecurityStatus(status);
		}
		op.setParams(params);
		ensureSecurity(op, status);
		if( logger.isDebugEnabled() )
		{
			logger.debug(op.getClass().getName());
		}
		return op.execute();
	}

	private String getPrivCacheId(ItemKey itemId)
	{
		UserState user = CurrentUser.getUserState();
		if( user.isGuest() )
		{
			AutoLogin settings = configurationService.getProperties(new AutoLogin());
			if( !settings.isEnableIpReferAcl() )
			{
				// No referrer Acls, cache for ALL guests
				return itemId.toString() + ":guest";
			}
			else
			{
				// Cache on referrer as well.  May or may not be useful.
				// It would help for serving up sub files in an item for example (IMS, or HTML files requesting sub-files)
				return itemId.toString() + ":guest:" + user.getIpAddress() + user.getHostReferrer();
			}
		}
		return itemId.toString() + ':' + CurrentUser.getSessionID();
	}

	private void ensureSecurity(WorkflowOperation op, SecurityStatus status)
	{
		Item item = op.getItem();

		// freakin proxies
		Set<Annotation> annotations = new HashSet<Annotation>();
		annotations.addAll(Arrays.asList(op.getClass().getAnnotations()));
		for( Class<?> iface : op.getClass().getInterfaces() )
		{
			annotations.addAll(Arrays.asList(iface.getAnnotations()));
		}

		for( Annotation annotation : annotations )
		{
			if( annotation instanceof SecureOnCall )
			{
				checkOnCall((SecureOnCall) annotation, status, item);
			}
			else if( annotation instanceof SecureAllOnCall )
			{
				SecureAllOnCall oncalls = (SecureAllOnCall) annotation;
				for( SecureOnCall oncall : oncalls.value() )
				{
					checkOnCall(oncall, status, item);
				}
			}
			else if( annotation instanceof SecureInModeration )
			{
				if( item == null || !item.isModerating() )
				{
					throw new AccessDeniedException(
						CurrentLocale.get("com.tle.core.services.item.error.requiredmoderation"));
				}
			}
			else if( annotation instanceof SecureNotInModeration )
			{
				if( item == null || item.isModerating() )
				{
					throw new AccessDeniedException(
						CurrentLocale.get("com.tle.core.services.item.error.requirednotmoderation"));
				}
			}
			else if( annotation instanceof SecureItemStatus )
			{
				SecureItemStatus secureAnn = (SecureItemStatus) annotation;
				ItemStatus[] statuses = secureAnn.value();
				List<ItemStatus> statusList = Arrays.asList(statuses);
				if( !secureAnn.not() && (item == null || !statusList.contains(item.getStatus())) )
				{
					throw new AccessDeniedException(
						CurrentLocale.get("com.tle.core.services.item.error.requiredoneof", statusList.toString()));
				}
				else if( secureAnn.not() && (item == null || statusList.contains(item.getStatus())) )
				{
					throw new AccessDeniedException(
						CurrentLocale.get("com.tle.core.services.item.error.requirednoneof", statusList.toString()));
				}
			}
		}
	}

	private void checkOnCall(SecureOnCall oncall, SecurityStatus status, Item item)
	{
		final String priv = oncall.priv();
		if( !status.getAllowedPrivileges().contains(priv) )
		{
			String itemIdString = "";
			if( item != null )
			{
				itemIdString = item.getItemId().toString();
			}
			throw new AccessDeniedException(
				CurrentLocale.get("com.tle.core.services.item.error.nopriv", priv, itemIdString));
		}
	}

	@Override
	@Transactional
	public ItemPack operation(ItemKey key, WorkflowOperation... operations)
	{
		ItemOperationParams params = new ItemOperationParamsImpl();
		ItemPack pack = runOperation(key, params, operations);
		postProcessParameters(params);
		return pack;
	}

	@Override
	public Set<String> getItemPrivsFast(ItemKey itemId)
	{
		final Set<String> privs = aclManager.filterNonGrantedPrivileges(getUnsecureReadOnly(itemId), itemPrivileges);
		privilegeCache.put(getPrivCacheId(itemId), privs);
		return privs;
	}

	/*
	@Override
	public void userDeletedEvent(UserDeletedEvent event)
	{
		operateAll(filterFactory.userDeleted(event.getUserID()), null);
	}
	
	@Override
	public void userEditedEvent(UserEditEvent event)
	{
		// Nothing to do here
	}
	
	@Override
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		operateAll(filterFactory.changeUserId(event.getFromUserId(), event.getToUserId()));
	}
	*/

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void executeOperationsNow(ItemOperationParams params, Collection<WorkflowOperation> operations)
	{
		if( operations != null )
		{
			WorkflowOperation[] opArr = operations.toArray(new WorkflowOperation[operations.size()]);
			execute(opArr, params, params.getSecurityStatus());
		}
	}

	@Override
	public List<WorkflowOperation> executeExtensionOperationsNow(ItemOperationParams params, String type)
	{
		List<WorkflowOperation> ops = getOperationListByType(type);
		executeOperationsNow(params, ops);
		return ops;
	}

	@Override
	public List<WorkflowOperation> executeExtensionOperationsLater(ItemOperationParams params, String type)
	{
		List<WorkflowOperation> ops = getOperationListByType(type);
		for( WorkflowOperation op : ops )
		{
			params.addOperation(op);
		}
		return ops;
	}

	@Override
	public boolean isAnOwner(Item item, String userUuid)
	{
		if( item.getOwner().equals(userUuid) )
		{
			return true;
		}

		return item.getCollaborators().contains(userUuid);
	}

	@Override
	public UserBean getOwner(Item item)
	{
		UserBean owner = null;
		String ownerID = item.getOwner();
		try
		{
			owner = userService.getInformationForUser(ownerID);
		}
		catch( Exception e )
		{
			logger.error("Error getting user", e);
		}

		if( owner == null )
		{
			owner = new DefaultUserBean(ownerID, ownerID, ownerID, ownerID, ownerID);
		}

		return owner;
	}

	@Override
	public ScriptContext createScriptContext(ItemPack itemPack, FileHandle fileHandle, Map<String, Object> attributes,
		Map<String, Object> extraObjects)
	{
		WorkflowScriptContextParams params = new WorkflowScriptContextParams(this, itemPack, fileHandle, attributes);

		ScriptContext scriptContext = scriptingService.createScriptContext(params);

		// now add our own bits...
		Map<String, Object> objects = Maps.newHashMap();
		if( extraObjects != null )
		{
			objects.putAll(extraObjects);
		}
		for( WorkflowScriptObjectContributor contrib : scriptObjectTracker.getBeanList() )
		{
			contrib.addWorkflowScriptObjects(objects, params);
		}
		for( Map.Entry<String, Object> entry : objects.entrySet() )
		{
			scriptContext.addScriptObject(entry.getKey(), entry.getValue());
		}

		return scriptContext;
	}

	@Override
	public void addWorkflowScriptObjects(Map<String, Object> objects, ScriptContextCreationParams params)
	{
		Map<String, Object> attributes = params.getAttributes();

		PropBagEx newXml = (PropBagEx) attributes.get(WorkflowScriptConstants.NEW_XML);
		if( newXml != null )
		{
			objects.put(WorkflowScriptConstants.NEW_XML, new PropBagWrapper(newXml));
		}
	}

	@Override
	public boolean evaluateScript(String script, String scriptName, ScriptContext context)
	{
		return scriptingService.evaluateScript(script, scriptName, context);
	}

	@Override
	public Object executeScript(String script, String scriptName, ScriptContext context, boolean function)
	{
		return scriptingService.executeScript(script, scriptName, context, function);
	}

	@Override
	public Object executeScript(String script, String scriptName, ScriptContext context, boolean function,
		Class<?> expecedReturnType)
	{
		return scriptingService.executeScript(script, scriptName, context, function, expecedReturnType);
	}

	@Override
	public void updateMetadataBasedSecurity(PropBagEx itemxml, Item item)
	{
		Workflow workflow = item.getItemDefinition().getWorkflow();
		WorkflowTaskDynamicTarget itemTask = new WorkflowTaskDynamicTarget(item.getId());

		List<TargetListEntry> entries = acquireTargetListEntries(workflow, itemxml, item);

		TargetList targetList = new TargetList();
		targetList.setPartial(true);
		targetList.setEntries(entries);
		aclManager.setTargetList(Node.WORKFLOW_TASK, itemTask, targetList);

		setItemMetadataSecurityTargets(itemxml, item);

		// Dynamic ACLs
		List<DynamicMetadataRule> dynamicMetadataRules = item.getItemDefinition().getDynamicMetadataRules();

		if( !Check.isEmpty(dynamicMetadataRules) )
		{
			// Map of Lists<Privilege, List<Pair<Grant/Revoke, UserID>>>
			Multimap<String, Pair<Boolean, String>> privMultimap = ArrayListMultimap.create();

			for( DynamicMetadataRule rule : dynamicMetadataRules )
			{
				List<String> ids = Lists.newArrayList(itemxml.getNodeList(rule.getPath()));

				for( String id : ids )
				{
					for( TargetListEntry tle : rule.getTargetList().getEntries() )
					{
						privMultimap.put(tle.getPrivilege(), new Pair<Boolean, String>(tle.isGranted(),
							SecurityConstants.getRecipient(SecurityConstants.getRecipientType(rule.getType()), id)));
					}
				}
			}

			List<TargetListEntry> tlEntries = Lists.newArrayList();

			// Create target list
			for( String privilege : privMultimap.keySet() )
			{
				Collection<Pair<Boolean, String>> listOfGrants = privMultimap.get(privilege);

				if( !Check.isEmpty(listOfGrants) )
				{
					getTargetListEntriesFromGrants(tlEntries, listOfGrants, privilege);
				}
			}
			TargetList dynaMetaTargetList = new TargetList(tlEntries);

			aclManager.setTargetList(Node.DYNAMIC_ITEM_METADATA, new ItemDynamicMetadataTarget(item),
				dynaMetaTargetList);
		}
		else
		{
			aclManager.setTargetList(Node.DYNAMIC_ITEM_METADATA, new ItemDynamicMetadataTarget(item), null);
		}
	}

	// Sonar - cyclomatic complexity reduction ...
	private List<TargetListEntry> acquireTargetListEntries(Workflow workflow, PropBagEx itemxml, Item item)
	{
		List<TargetListEntry> entries = new ArrayList<TargetListEntry>();
		if( workflow != null && item.isModerating() )
		{
			Map<String, WorkflowItem> items = workflow.getAllWorkflowItems();
			for( WorkflowItem task : items.values() )
			{
				String taskId = task.getUuid();
				String path = task.getUserPath();
				if( !Check.isEmpty(path) )
				{
					for( String userId : itemxml.getNodeList(path) )
					{
						String recipient = SecurityConstants.getRecipient(Recipient.USER, userId);
						entries.add(new TargetListEntry(true, false, "MODERATE_ITEM", recipient, taskId));
						entries.add(new TargetListEntry(true, false, "VIEW_ITEM", recipient, taskId));
						if( task.isAllowEditing() )
						{
							entries.add(new TargetListEntry(true, false, "EDIT_ITEM", recipient, taskId));
						}
					}
				}
			}
		}
		return entries;
	}

	// Sonar - cyclomatic complexity reduction ...
	private void setItemMetadataSecurityTargets(PropBagEx itemxml, Item item)
	{
		List<String> results = Lists.newArrayList();
		List<ItemMetadataRule> itemMetadataRules = item.getItemDefinition().getItemMetadataRules();

		if( itemMetadataRules != null )
		{
			for( ItemMetadataRule rule : itemMetadataRules )
			{
				ItemPack itemPack = new ItemPack();
				itemPack.setItem(item);
				itemPack.setXml(itemxml);
				ScriptContext context = createScriptContext(itemPack, null, null, null);
				if( evaluateScript(rule.getScript(), ITEM_METADATA_SECURITY_RULE_SCRIPT_NAME + rule.getName(),
					context) )
				{
					results.add(rule.getId());
				}
			}
		}
		item.setMetadataSecurityTargets(results);
	}

	// Sonar - cyclomatic complexity reduction ...
	private void getTargetListEntriesFromGrants(List<TargetListEntry> tlEntries,
		Collection<Pair<Boolean, String>> listOfGrants, String privilege)
	{
		TargetListEntry tle = null;
		Boolean last = null;
		int count = 0;

		for( Pair<Boolean, String> privEntry : listOfGrants )
		{
			Boolean grant = privEntry.getFirst();
			String who = privEntry.getSecond();

			if( last == null )
			{
				last = grant;
				tle = new TargetListEntry(grant.booleanValue(), false, privilege, who);
				count++;
			}
			else if( last.equals(grant) && tle != null )
			{
				String existingWho = tle.getWho();
				tle.setWho(existingWho + (count > 1 ? " OR " + who : " " + who));
				count++;
			}
			else
			{
				tlEntries.add(tle);
				last = grant;
				tle = new TargetListEntry(grant.booleanValue(), false, privilege, who);
				count = 0;
			}
		}

		if( tle != null && count > 1 )
		{
			tle.setWho(tle.getWho() + " OR");
		}

		tlEntries.add(tle);
	}
}