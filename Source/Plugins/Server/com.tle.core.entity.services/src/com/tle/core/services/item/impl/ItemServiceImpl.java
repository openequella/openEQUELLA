package com.tle.core.services.item.impl;

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
import com.dytech.edge.exceptions.AttachmentNotFoundException;
import com.dytech.edge.exceptions.ItemNotFoundException;
import com.dytech.edge.exceptions.WorkflowException;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.tle.beans.ReferencedURL;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.ItemDefinition;
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
import com.tle.common.ItemIdExtension;
import com.tle.common.Triple;
import com.tle.common.collection.AttachmentConfigConstants;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.util.Logger;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.dao.ItemDao;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.events.ItemOperationBatchEvent;
import com.tle.core.events.ItemOperationEvent;
import com.tle.core.events.UpdateReferencedUrlsEvent;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.ItemOperationBatchListener;
import com.tle.core.events.listeners.ItemOperationListener;
import com.tle.core.events.listeners.UpdateReferencedUrlsListener;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.guice.Bind;
import com.tle.core.notification.NotificationService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.TLEAclManager;
import com.tle.core.security.impl.SecureAllOnCall;
import com.tle.core.security.impl.SecureInModeration;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureNotInModeration;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.services.EventService;
import com.tle.core.services.LockingService;
import com.tle.core.services.LoggingService;
import com.tle.core.services.item.ItemResolverExtension;
import com.tle.core.services.item.ItemService;
import com.tle.core.url.URLCheckerService;
import com.tle.core.url.URLCheckerService.URLCheckMode;
import com.tle.core.url.URLEvent;
import com.tle.core.url.URLEvent.URLEventType;
import com.tle.core.url.URLListener;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;
import com.tle.core.util.ItemHelper;
import com.tle.core.util.ItemHelper.ItemHelperSettings;
import com.tle.core.workflow.filters.FilterFactory;
import com.tle.core.workflow.filters.FilterResultListener;
import com.tle.core.workflow.filters.WorkflowFilter;
import com.tle.core.workflow.filters.WorkflowFilter.FilterResults;
import com.tle.core.workflow.operations.DeleteOperation;
import com.tle.core.workflow.operations.PurgeOperation;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.core.workflow.operations.WorkflowOperation;
import com.tle.core.workflow.operations.WorkflowParams;
import com.tle.exceptions.AccessDeniedException;

/*
 * @author Nicholas Read
 */
@Bind(ItemService.class)
@Singleton
public class ItemServiceImpl
	implements
		ItemService,
		ItemOperationListener,
		ItemOperationBatchListener,
		UserChangeListener,
		UpdateReferencedUrlsListener,
		URLListener,
		ItemResolverExtension
{
	private final Random mdcNums = new Random();
	private final Cache<String, Set<String>> privilegeCache = CacheBuilder.newBuilder().softValues()
		.expireAfterAccess(30, TimeUnit.SECONDS).build();

	@Inject
	private ItemDao dao;
	@Inject
	private LockingService lockingService;
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private AuditLogService auditLogService;
	private Logger logger;
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private NotificationService notificationService;
	@Inject
	private EventService eventService;
	@Inject
	private FilterFactory filterFactory;
	@Inject
	private WorkflowFactory workflowFactory;
	@Inject
	private URLCheckerService urlCheckerService;

	// plugin extensions
	private PluginTracker<WorkflowOperation> operationTracker;
	private Map<String, List<Extension>> operationExtensionMap;

	private final Collection<String> itemPrivileges;
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
		return getInternal(itemId);
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
		return getInternal(key);
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.VIEW_ITEM)
	@Transactional(propagation = Propagation.REQUIRED, noRollbackFor = ItemNotFoundException.class)
	public Item get(ItemKey key)
	{
		return getInternal(key);
	}

	private Item getInternal(ItemKey key)
	{
		key = ensureItemKey(key);
		Item item = dao.get(key);
		if( item == null )
		{
			String message = CurrentLocale.get("com.tle.core.services.item.error.itemnotfound", key); //$NON-NLS-1$
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
		Item item = getInternal(key);
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
		return getItemXmlPropBag(getInternal(key));
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
		logger.info("Updated " + count + " items"); //$NON-NLS-1$ //$NON-NLS-2$
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

	@Override
	public long getUserFileSize(String whereClause, String[] names, Object[] values)
	{
		return dao.getUserFileSize(whereClause, names, values);
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
			lockingService.unlockItem(item, true);
		}
		catch( LockedException e )
		{
			logger.warn(CurrentLocale.get("com.tle.core.services.item.error.stilllockedexception"), e); //$NON-NLS-1$
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

	@SuppressWarnings("nls")
	@Transactional
	protected ItemPack runOperation(ItemKey key, WorkflowParams params, WorkflowOperation... operations)
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
			logger.error("Error performing workflow on item: " + key, t); //$NON-NLS-1$
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

	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public void operateAll(WorkflowFilter filter)
	{
		operateAllPrivate(filter, null);
	}

	@Override
	@Transactional
	public void operateAllInTransaction(WorkflowFilter filter)
	{
		operateAllPrivate(filter, null);
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public void operateAll(final WorkflowFilter filter, final FilterResultListener listener)
	{
		operateAllPrivate(filter, listener);
	}

	private WorkflowParams operateAllPrivate(final WorkflowFilter filter, final FilterResultListener listener)
	{
		final WorkflowParams params = new WorkflowParams();
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
		logger.info("Finished operateAll on: " + filter.getClass()); //$NON-NLS-1$
		return params;
	}

	private void processItemId(final WorkflowFilter filter, final FilterResultListener listener,
		final WorkflowParams params, final boolean containsDelete, final ItemKey itemId)
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
				listener.failed(itemId, original, Throwables.getRootCause(e));
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
			if( op instanceof DeleteOperation || op instanceof PurgeOperation )
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

	protected void initBundle(LanguageBundle bundle)
	{
		if( bundle != null )
		{
			Hibernate.initialize(bundle.getStrings());
		}
	}

	protected void perItemPostProcess(final WorkflowParams params)
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

	private void postProcessParameters(final WorkflowParams params)
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
	protected void afterCommitAll(WorkflowParams params)
	{
		if( params.isNotificationsAdded() )
		{
			notificationService.processEmails();
		}
		for( ApplicationEvent<?> event : params.getAfterCommitAllEvents() )
		{
			eventService.publishApplicationEvent(event);
		}

		List<WorkflowFilter> filters = params.getExtraFilters();
		if( filters != null )
		{
			for( WorkflowFilter filter : filters )
			{
				try
				{
					operateAll(filter);
				}
				catch( WorkflowException e )
				{
					logger.error("Error running filter", e); //$NON-NLS-1$
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
			logger.error(CurrentLocale.get("com.tle.core.services.item.error.operation"), ex); //$NON-NLS-1$
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

	private void execute(WorkflowOperation[] operations, WorkflowParams params, SecurityStatus securityStatus)
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

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.item.ItemService#execute(com.tle.core.workflow.
	 * operations.WorkflowOperationInterface, com.tle.beans.item.Item,
	 * com.dytech.devlib.PropBagEx,
	 * com.tle.core.workflow.operations.WorkflowParams)
	 */
	@SuppressWarnings("nls")
	private boolean execute(WorkflowOperation op, WorkflowParams params, SecurityStatus status)

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
					status.setLock(lockingService.getLock(item));
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
						CurrentLocale.get("com.tle.core.services.item.error.requiredmoderation")); //$NON-NLS-1$
				}
			}
			else if( annotation instanceof SecureNotInModeration )
			{
				if( item == null || item.isModerating() )
				{
					throw new AccessDeniedException(
						CurrentLocale.get("com.tle.core.services.item.error.requirednotmoderation")); //$NON-NLS-1$
				}
			}
			else if( annotation instanceof SecureItemStatus )
			{
				SecureItemStatus secureAnn = (SecureItemStatus) annotation;
				ItemStatus[] statuses = secureAnn.value();
				List<ItemStatus> statusList = Arrays.asList(statuses);
				if( !secureAnn.not() && (item == null || !statusList.contains(item.getStatus())) )
				{
					throw new AccessDeniedException(CurrentLocale.get(
						"com.tle.core.services.item.error.requiredoneof", statusList.toString())); //$NON-NLS-1$
				}
				else if( secureAnn.not() && (item == null || statusList.contains(item.getStatus())) )
				{
					throw new AccessDeniedException(CurrentLocale.get(
						"com.tle.core.services.item.error.requirednoneof", statusList.toString())); //$NON-NLS-1$
				}
			}
		}
	}

	private void checkOnCall(SecureOnCall oncall, SecurityStatus status, Item item)
	{
		final String priv = oncall.priv();
		if( !status.getAllowedPrivileges().contains(priv) )
		{
			String itemIdString = ""; //$NON-NLS-1$
			if( item != null )
			{
				itemIdString = item.getItemId().toString();
			}
			throw new AccessDeniedException(CurrentLocale.get(
				"com.tle.core.services.item.error.nopriv", priv, itemIdString)); //$NON-NLS-1$
		}
	}

	@Override
	@Transactional
	public ItemPack operation(ItemKey key, WorkflowOperation... operations)
	{
		WorkflowParams params = new WorkflowParams();
		ItemPack pack = runOperation(key, params, operations);
		postProcessParameters(params);
		return pack;
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

	@Override
	public void delete(Item item)
	{
		auditLogService.logItemPurged(item);
		dao.delete(item);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void executeOperationsNow(WorkflowParams params, Collection<WorkflowOperation> operations)

	{
		if( operations != null )
		{
			WorkflowOperation[] opArr = operations.toArray(new WorkflowOperation[operations.size()]);
			execute(opArr, params, params.getSecurityStatus());
		}
	}

	@Override
	public List<WorkflowOperation> executeExtensionOperationsNow(WorkflowParams params, String type)

	{
		List<WorkflowOperation> ops = getOperationListByType(type);
		executeOperationsNow(params, ops);
		return ops;
	}

	@Override
	public List<WorkflowOperation> executeExtensionOperationsLater(WorkflowParams params, String type)
	{
		List<WorkflowOperation> ops = getOperationListByType(type);
		for( WorkflowOperation op : ops )
		{
			params.addOperation(op);
		}
		return ops;
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

	@SuppressWarnings("nls")
	@Inject
	public void setPluginService(PluginService pluginService)
	{
		operationTracker = new PluginTracker<WorkflowOperation>(pluginService, "com.tle.core.workflow", "operation",
			null, new PluginTracker.ExtensionParamComparator("order"));
		operationTracker.setBeanKey("class");
		idTracker = new PluginTracker<ItemIdExtension>(pluginService, getClass(), "itemIdExtension", "id")
			.setBeanKey("bean");
	}

	private List<WorkflowOperation> getOperationListByType(String type)
	{
		List<Extension> extensions = getExtensionListByType(type);
		List<WorkflowOperation> ops = new ArrayList<WorkflowOperation>();
		if( extensions != null )
		{
			for( Extension extension : extensions )
			{
				WorkflowOperation op = operationTracker.getBeanByParameter(extension, "class"); //$NON-NLS-1$
				ops.add(op);
			}
		}
		return ops;
	}

	private synchronized List<Extension> getExtensionListByType(String type)
	{
		if( operationExtensionMap == null || operationTracker.needsUpdate() )
		{
			operationExtensionMap = new HashMap<String, List<Extension>>();
			List<Extension> allExtension = operationTracker.getExtensions();
			for( Extension extension : allExtension )
			{
				String extType = extension.getParameter("type").valueAsString(); //$NON-NLS-1$
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
	public void delete(ItemId itemId, boolean purge, boolean waitForIndex)
	{
		Item item = dao.getExistingItem(itemId);
		if( item.getStatus() == ItemStatus.DELETED )
		{
			if( !purge )
			{
				return;
			}
			operation(itemId, workflowFactory.purge(waitForIndex));
			return;
		}
		WorkflowOperation secondOp;
		if( purge )
		{
			secondOp = workflowFactory.purge(waitForIndex);
		}
		else
		{
			secondOp = workflowFactory.save();
		}
		operation(itemId, workflowFactory.delete(), secondOp);
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
	public void updateReferencedUrlsEvent(UpdateReferencedUrlsEvent event)
	{
		operation(event.getItemKey(),
			new WorkflowOperation[]{workflowFactory.updateReferencedUrls(), workflowFactory.reIndexIfRequired(),});
	}

	@Override
	public void urlEvent(URLEvent event)
	{
		// Notify users of URLs reaching the warning or disabled stage.
		URLEventType type = event.getType();
		if( type == URLEventType.URL_WARNING || type == URLEventType.URL_DISABLED )
		{
			final ReferencedURL rurl = urlCheckerService.getUrlStatus(event.getUrl(), URLCheckMode.RECORDS_ONLY);
			operateAll(filterFactory.notifyBadUrl(rurl));
		}
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
		return !canViewRestrictedAttachments(item);
	}

	@Override
	public boolean canViewRestrictedAttachments(IItem<?> item)
	{
		return !aclManager.filterNonGrantedPrivileges(item, AttachmentConfigConstants.VIEW_RESTRICTED_ATTACHMENTS)
			.isEmpty();
	}

	@Override
	public boolean canRestrictAttachments(IItem<?> item)
	{
		return !aclManager.filterNonGrantedPrivileges(item, AttachmentConfigConstants.RESTRICT_ATTACHMENTS).isEmpty();
	}
}