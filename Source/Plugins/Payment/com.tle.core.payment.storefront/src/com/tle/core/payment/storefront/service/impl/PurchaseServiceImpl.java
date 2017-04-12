package com.tle.core.payment.storefront.service.impl;

import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.exceptions.WebException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Provider;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.i18n.LangUtils;
import com.tle.common.interfaces.BaseEntityReference;
import com.tle.common.interfaces.I18NStrings;
import com.tle.common.payment.storefront.entity.Order;
import com.tle.common.payment.storefront.entity.OrderStorePart;
import com.tle.common.payment.storefront.entity.Purchase;
import com.tle.common.payment.storefront.entity.PurchaseItem;
import com.tle.common.payment.storefront.entity.PurchasedContent;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.item.serializer.ItemDeserializerService;
import com.tle.core.payment.beans.store.StoreHarvestableItemBean;
import com.tle.core.payment.beans.store.StoreHarvestableItemsBean;
import com.tle.core.payment.beans.store.StorePriceBean;
import com.tle.core.payment.beans.store.StoreTransactionBean;
import com.tle.core.payment.beans.store.StoreTransactionItemBean;
import com.tle.core.payment.storefront.dao.PurchaseDao;
import com.tle.core.payment.storefront.dao.PurchaseItemDao;
import com.tle.core.payment.storefront.dao.PurchasedContentDao;
import com.tle.core.payment.storefront.operation.OperationFactory;
import com.tle.core.payment.storefront.service.PurchaseService;
import com.tle.core.payment.storefront.service.ShopService;
import com.tle.core.payment.storefront.service.StoreService;
import com.tle.core.payment.storefront.settings.StoreFrontSettings;
import com.tle.core.payment.storefront.task.CheckPurchasesTask;
import com.tle.core.payment.storefront.task.PurchaseMessage;
import com.tle.core.payment.storefront.task.PurchaseMessage.Type;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.services.StagingService;
import com.tle.core.services.TaskService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.services.impl.Task;
import com.tle.core.services.item.ItemService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;
import com.tle.core.workflow.CloneFileProcessingExtension;
import com.tle.core.workflow.operations.AbstractCloneOperation;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;

@Bind(PurchaseService.class)
@Singleton
public class PurchaseServiceImpl implements PurchaseService, UserChangeListener, ScheduledTask
{
	private static final Logger LOGGER = Logger.getLogger(PurchaseService.class);

	@Inject
	private PurchaseItemDao purchaseItemDao;
	@Inject
	private PurchaseDao purchaseDao;
	@Inject
	private PurchasedContentDao purchasedContentDao;

	@Inject
	private WorkflowFactory workflowFactory;
	@Inject
	private OperationFactory operationFactory;
	@Inject
	private ItemService itemService;
	@Inject
	private StagingService stagingService;
	@Inject
	private ShopService shopService;
	@Inject
	private StoreService storeService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private ItemDeserializerService itemDeserializerService;
	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private TaskService taskService;

	private static final long DEFAULT_TIMEOUT = TimeUnit.MINUTES.toMillis(1);

	@Inject
	private Provider<CheckPurchasesTask> taskProvider;
	private CheckPurchasesTask localTask;

	// A bit yuck
	private PluginTracker<CloneFileProcessingExtension> fileProcessorTracker;

	private String taskId;

	public Task createTask()
	{
		localTask = taskProvider.get();
		return localTask;
	}

	@Override
	@Transactional
	public void downloadHarvestableItem(Store store, StoreHarvestableItemBean storeItemBean)
	{
		boolean skip = false;
		ItemId sourceItemId = new ItemId(storeItemBean.getUuid(), storeItemBean.getVersion());

		PurchasedContent purchasedContent = purchasedContentDao.getForSourceItem(store, sourceItemId);

		if( purchasedContent == null )
		{
			purchasedContent = purchasedContentDao.getForSourceUuid(store, storeItemBean.getUuid());
		}
		else
		{
			skip = true;
		}

		// if item is purchased but has been suspended, then un-suspend it (this
		// is because resumeAllExistingItems only checks subscriptions)
		if( purchasedContent != null )
		{
			final ItemId itemId = new ItemId(purchasedContent.getItemUuid(), purchasedContent.getItemVersion());
			resumeIfSuspended(itemService.getUnsecure(itemId));
		}

		if( !skip )
		{
			StagingFile stagingFile = stagingService.createStagingArea();

			EquellaItemBean equellaItemBean = new EquellaItemBean();
			equellaItemBean.setAttachments(storeItemBean.getAttachments());

			String uuid = (purchasedContent == null) ? UUID.randomUUID().toString() : purchasedContent.getItemUuid();
			equellaItemBean.setUuid(uuid);

			equellaItemBean.setName(storeItemBean.getName());
			equellaItemBean.setDescription(storeItemBean.getDescription());
			equellaItemBean.setNameStrings(storeItemBean.getNameStrings());
			equellaItemBean.setDescriptionStrings(storeItemBean.getDescriptionStrings());
			equellaItemBean.setNavigation(storeItemBean.getNavigation());

			StoreFrontSettings settings = configService.getProperties(new StoreFrontSettings());
			String collectionUuid = settings.getCollection();
			ItemDefinition collection = itemDefinitionService.getByUuid(collectionUuid);
			if( collection == null )
			{
				throw new RuntimeException("Please select a valid storefront collection");
			}

			Schema schema = collection.getSchema();
			String namePath = schema.getItemNamePath();
			String descriptionPath = schema.getItemDescriptionPath();

			PropBagEx metadata = new PropBagEx("<xml/>"); //$NON-NLS-1$

			I18NStrings nameStrings = storeItemBean.getNameStrings();
			if( nameStrings == null || nameStrings.getStrings().size() == 1 )
			{
				if( storeItemBean.getName() != null )
				{
					metadata.setNode(namePath, storeItemBean.getName().toString());
				}
			}
			else if( nameStrings.getStrings().size() > 1 )
			{
				LanguageBundle bundle = LangUtils.createTextTempLangugageBundle(nameStrings.getStrings());
				LangUtils.setBundleToXml(bundle, metadata.aquireSubtree(namePath));
			}

			I18NStrings descStrings = storeItemBean.getDescriptionStrings();
			if( descStrings == null || descStrings.getStrings().size() == 1 )
			{
				if( storeItemBean.getDescription() != null )
				{
					metadata.setNode(descriptionPath, storeItemBean.getDescription().toString());
				}
			}
			else if( descStrings.getStrings().size() > 1 )
			{
				LanguageBundle bundle = LangUtils.createTextTempLangugageBundle(descStrings.getStrings());
				LangUtils.setBundleToXml(bundle, metadata.aquireSubtree(descriptionPath));
			}

			PropBagEx storeMetadata = new PropBagEx(storeItemBean.getMetadata());
			metadata.append("/storedata", storeMetadata);
			equellaItemBean.setMetadata(metadata.toString());
			equellaItemBean.setCollection(new BaseEntityReference(collectionUuid));

			shopService.downloadItemFiles(store, stagingFile, storeItemBean);

			ItemIdKey newItem = itemDeserializerService.newItem(equellaItemBean, stagingFile.getUuid(), false, true,
				true);
			// note: commit files hasn't happened yet
			for( CloneFileProcessingExtension fileProcessor : fileProcessorTracker.getBeanList() )
			{
				fileProcessor.processFiles(sourceItemId, stagingFile, itemService.getUnsecure(newItem), stagingFile);
			}

			purchasedContent = new PurchasedContent();
			purchasedContent.setItemUuid(newItem.getUuid());
			purchasedContent.setItemVersion(newItem.getVersion());
			purchasedContent.setSourceItemUuid(sourceItemId.getUuid());
			purchasedContent.setSourceItemVersion(sourceItemId.getVersion());
			purchasedContent.setInstitution(CurrentInstitution.get());
			purchasedContent.setStore(store);
			purchasedContentDao.save(purchasedContent);

			itemService.operation(newItem, operationFactory.itemUpdated(), workflowFactory.reIndexIfRequired());

		}
	}

	@Override
	public boolean downloadNewHarvestableItems(Store store)
	{
		// allow other items to be processed but return failed
		boolean success = true;
		// Download any new purchases
		boolean moreResults = true;
		int start = 0;
		int length = 10;

		while( moreResults )
		{
			StoreHarvestableItemsBean harvestableItems = shopService.listHarvestableItems(store, null, start, length,
				true);
			List<StoreHarvestableItemBean> results = harvestableItems.getResults();
			for( StoreHarvestableItemBean storeHarvestableItemBean : results )
			{
				try
				{
					StoreHarvestableItemBean harvestableItemBean = shopService.getFullItem(store,
						storeHarvestableItemBean.getUuid());
					downloadHarvestableItem(store, harvestableItemBean);
				}
				catch( Exception e )
				{
					LOGGER.error("Error downloading purchased item", e);
					success = false;
				}
			}

			start += length;
			moreResults = harvestableItems.getAvailable() > start;
		}

		return success;
	}

	@Override
	public boolean downloadUpdatedHarvestableItems(Store store, Date lastHarvest)
	{
		// allow other items to be processed but return failed
		boolean success = true;

		// Download any new versions
		boolean moreResults = true;
		int start = 0;
		int length = 10;

		while( moreResults )
		{
			StoreHarvestableItemsBean harvestableItems = shopService.listHarvestableItems(store, lastHarvest, start,
				length, false);
			List<StoreHarvestableItemBean> results = harvestableItems.getResults();
			for( StoreHarvestableItemBean storeHarvestableItemBean : results )
			{
				try
				{
					StoreHarvestableItemBean harvestableItemBean = shopService.getFullItem(store,
						storeHarvestableItemBean.getUuid());
					downloadHarvestableItem(store, harvestableItemBean);
				}
				catch( Exception e )
				{
					LOGGER.error(e.getMessage(), e);
					success = false;
				}
			}

			start += length;
			moreResults = harvestableItems.getAvailable() > start;
		}
		return success;
	}

	@Override
	public boolean suspendAllExpiredItems(Store store, Date lastHarvest)
	{
		// allow other items to be processed but return failed
		boolean success = true;
		boolean moreResults = true;
		int start = 0;
		int length = 10;
		Date now = new Date();
		while( moreResults )
		{

			StoreHarvestableItemsBean harvestableItems = shopService.listExpiredItems(store, lastHarvest, now, start,
				length);
			List<StoreHarvestableItemBean> results = harvestableItems.getResults();
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug("Found " + harvestableItems.getAvailable() + " expired items");
			}
			for( StoreHarvestableItemBean storeHarvestableItemBean : results )
			{
				// TODO: does this UUID exist in another shop?
				// See http://dev.equella.com/issues/7635#note-5

				try
				{
					suspendExpiredItem(store, storeHarvestableItemBean.getUuid());
				}
				catch( Exception e )
				{
					LOGGER.error("Error suspending expired item", e);
					success = false;
				}
			}

			start += length;
			moreResults = harvestableItems.getAvailable() > start;
		}

		LOGGER.debug("Checking resources we believe are expired");
		final List<String> assumedInactive = getAssumedInactivePurchases(store);
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("Found " + assumedInactive.size() + " assumed expired resources");
		}
		for( String sourceItemUuid : assumedInactive )
		{
			try
			{
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Attempting to suspend resource with store-side UUID of " + sourceItemUuid);
				}
				suspendExpiredItem(store, sourceItemUuid);
			}
			catch( Exception e )
			{
				LOGGER.error("Error suspending expired item", e);
				success = false;
			}
		}

		return success;
	}

	/**
	 * @param store
	 * @return A list of source (store) item UUIDs where we think it should be
	 *         expired
	 */
	@Transactional
	public List<String> getAssumedInactivePurchases(Store store)
	{
		final List<String> inactives = Lists.newArrayList();

		// We know some of our resources are not expired but, according to us,
		// really should be.
		// Get these one-by-one and expire them if the store agrees with us.
		final List<PurchasedContent> livePurchases = purchasedContentDao.getLivePurchases(store);
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("Found " + livePurchases.size() + " current live purchases");
		}
		for( PurchasedContent pcon : livePurchases )
		{
			// check if it's an active subscription
			final String sourceItemUuid = pcon.getSourceItemUuid();
			final List<PurchaseItem> actives = purchaseItemDao.findActivePurchases(store, sourceItemUuid);
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug("Found " + actives.size() + " active purchases for remote UUID " + sourceItemUuid);
			}
			if( actives.isEmpty() )
			{
				inactives.add(sourceItemUuid);
			}
		}
		return inactives;
	}

	@Override
	public boolean resumeAllExistingItems(Store store)
	{
		// allow other items to be processed but return failed
		boolean success = true;
		boolean moreResults = true;
		int start = 0;
		int length = 10;
		Date now = new Date();
		while( moreResults )
		{
			StoreHarvestableItemsBean harvestableItems = shopService.listActiveItems(store, now, now, start, length);
			List<StoreHarvestableItemBean> results = harvestableItems.getResults();
			for( StoreHarvestableItemBean storeHarvestableItemBean : results )
			{
				try
				{
					resumeExistingItem(store, storeHarvestableItemBean.getUuid());
				}
				catch( Exception e )
				{
					LOGGER.error("Error resuming item", e);
					success = false;
				}
			}

			start += length;
			moreResults = harvestableItems.getAvailable() > start;
		}

		return success;
	}

	@Override
	@Transactional
	public void suspendExpiredItem(Store store, String sourceItemUuid)
	{
		// Make sure it's really expired
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("Making sure it's really expired");
		}
		if( !shopService.isHarvestable(store, sourceItemUuid) )
		{
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug(sourceItemUuid + " is indeed expired");
			}
			PurchasedContent purchasedContent = purchasedContentDao.getForSourceUuid(store, sourceItemUuid);
			LOGGER.debug("Finding the local resource to expire it");
			if( purchasedContent != null )
			{
				final String localUuid = purchasedContent.getItemUuid();
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Found local resource " + localUuid + ".  Getting all versions to suspend them.");
				}
				final List<Item> versionDetails = itemService.getVersionDetails(localUuid);
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Found " + versionDetails.size() + " versions of said resource");
				}
				for( Item item : versionDetails )
				{
					if( item.getStatus() != ItemStatus.SUSPENDED )
					{
						LOGGER.debug("Suspending " + item.getItemId().toString());
						itemService.operation(item.getItemId(), workflowFactory.suspend(), workflowFactory.save());
					}
					else
					{
						LOGGER.debug(item.getItemId().toString() + " is already suspended");
					}
				}
			}
			else
			{
				LOGGER.warn("Local resource for remote UUID of " + sourceItemUuid + " is not found!");
			}
		}
	}

	@Override
	@Transactional
	public void resumeExistingItem(Store store, String sourceItemUuid)
	{
		// make sure it's not expired
		boolean expired = false;
		try
		{
			shopService.getFullItem(store, sourceItemUuid);
		}
		catch( WebException e )
		{
			expired = e.getCode() == 302;
		}

		if( !expired )
		{
			PurchasedContent purchasedContent = purchasedContentDao.getForSourceUuid(store, sourceItemUuid);
			if( purchasedContent != null )
			{
				List<Item> versionDetails = itemService.getVersionDetails(purchasedContent.getItemUuid());
				for( Item item : versionDetails )
				{
					resumeIfSuspended(item);
				}
			}
		}
	}

	@Transactional(propagation = Propagation.MANDATORY)
	private void resumeIfSuspended(Item item)
	{
		if( item.getStatus() == ItemStatus.SUSPENDED )
		{
			itemService.operation(item.getItemId(), workflowFactory.resume(), workflowFactory.save());
		}
	}

	@Override
	public Set<Item> filterNonPurchased(Set<Item> items)
	{
		if( Check.isEmpty(items) )
		{
			return Collections.emptySet();
		}

		final Set<String> uuids = Sets.newHashSet();
		for( Item item : items )
		{
			uuids.add(item.getUuid());
		}
		final Set<String> purchased = purchasedContentDao.getPurchased(uuids);
		final Set<Item> result = Sets.newHashSet();
		for( Item item : items )
		{
			if( purchased.contains(item.getUuid()) )
			{
				result.add(item);
			}
		}
		return result;
	}

	@SuppressWarnings("nls")
	@Override
	public boolean isPurchased(String itemUuid)
	{
		long count = purchasedContentDao.countByCriteria(Restrictions.eq("institution", CurrentInstitution.get()),
			Restrictions.eq("itemUuid", itemUuid));
		return (count > 0);
	}

	@Override
	public boolean isSourcePurchased(Store store, String itemUuid)
	{
		return !purchaseItemDao.enumerateForSourceItem(itemUuid).isEmpty();

	}

	@Transactional
	@Override
	public List<PurchaseItem> enumerateForItem(ItemId itemId)
	{
		return purchaseItemDao.enumerateForItem(itemId);
	}

	@Override
	public List<PurchaseItem> enumerateForSourceItem(String uuid)
	{
		return purchaseItemDao.enumerateForSourceItem(uuid);
	}

	@Override
	public void checkDownloadableContentAndCheckSubscriptions()
	{
		LOGGER.trace("checkDownloadableContentAndCheckSubscriptions invoked");

		List<Store> stores = storeService.findAll();
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("Checking " + stores.size() + " stores for new items and expired subscriptions");
		}

		for( Store store : stores )
		{
			Date lastHarvest = store.getLastHarvest();
			final String storeName = store.getStoreUrl();
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug("Last harvest date for " + storeName + " was " + lastHarvest);
				LOGGER.debug(storeName + " is " + (store.isDisabled() ? "disabled" : "enabled"));
			}

			boolean success = true;
			boolean enabled = !store.isDisabled();

			try
			{
				if( enabled )
				{
					success &= downloadUpdatedHarvestableItems(store, lastHarvest);
					success &= downloadNewHarvestableItems(store);
				}

				success &= suspendAllExpiredItems(store, lastHarvest);

				if( enabled )
				{
					success &= resumeAllExistingItems(store);
				}
			}
			catch( Exception e )
			{
				LOGGER.error("Error contacting store", e);
				success = false;
			}

			if( success )
			{
				LOGGER.debug("Updating last harvest date for " + storeName);
				storeService.updateHarvestDate(store);
			}
		}
	}

	@Override
	public void startCheckDownloadableContentAndCheckSubscriptions()
	{
		taskService.postMessage(getTaskId(),
			new PurchaseMessage(Type.DOWNLOADS, CurrentInstitution.get().getUniqueId()));
	}

	@Override
	public void startCheckCurrentOrders()
	{
		taskService.postMessage(getTaskId(), new PurchaseMessage(Type.ORDERS, CurrentInstitution.get().getUniqueId()));
	}

	@Transactional
	@Override
	public Purchase createPurchase(OrderStorePart part, StoreTransactionBean transaction)
	{
		final Purchase purch = new Purchase();
		purch.setUuid(UUID.randomUUID().toString());
		final StoreTransactionBean.PaidStatus paidStatus = transaction.getPaidStatus();
		// It kind of has to be...
		if( paidStatus != null && paidStatus == StoreTransactionBean.PaidStatus.PAID )
		{
			purch.setPaid(true);
		}
		final Order order = part.getOrder();
		purch.setPaidDate(transaction.getPaidDate());
		purch.setCheckoutBy(order.getCreatedBy());
		purch.setCheckoutDate(order.getCreatedDate());
		purch.setInstitution(CurrentInstitution.get());
		purch.setPaidForBy(CurrentUser.getUserID());
		purch.setTransactionUuid(transaction.getUuid());
		purch.setReceipt(transaction.getReceipt());
		purch.setStore(part.getStore());

		final List<PurchaseItem> purchaseItems = Lists.newArrayList();
		for( StoreTransactionItemBean orderItem : transaction.getItems() )
		{
			final PurchaseItem pitem = new PurchaseItem();
			pitem.setPurchase(purch);
			pitem.setUuid(UUID.randomUUID().toString());
			final StoreHarvestableItemBean item = orderItem.getItem();
			pitem.setSourceItemUuid(item.getUuid());
			pitem.setSourceItemVersion(item.getVersion());
			StorePriceBean price = orderItem.getPrice();
			pitem.setPrice(price.getValue().getValue());
			pitem.setTax(price.getTaxValue().getValue());
			pitem.setCatalogueUuid(orderItem.getCatalogueUuid());
			pitem.setSubscriptionEndDate(orderItem.getSubscriptionEndDate());
			pitem.setSubscriptionStartDate(orderItem.getSubscriptionStartDate());
			pitem.setUsers(orderItem.getQuantity());

			purchaseItems.add(pitem);
		}
		purch.setPurchaseItems(purchaseItems);

		StorePriceBean price = transaction.getPrice();
		purch.setCurrency(Currency.getInstance(price.getCurrency()));
		purch.setPrice(price.getValue().getValue());

		purchaseDao.save(purch);
		return purch;
	}

	@Override
	public List<String> enumerateCheckoutByforItem(ItemId itemId)
	{
		return purchaseDao.enumerateCheckoutByforItem(itemId);
	}

	@Override
	public PurchaseItem getPurchaseItem(String purchaseItemUuid)
	{
		return purchaseItemDao.get(purchaseItemUuid);
	}

	@Override
	public void userDeletedEvent(UserDeletedEvent event)
	{
		// Don't care
	}

	@Override
	public void userEditedEvent(UserEditEvent event)
	{
		// Don't care
	}

	@Override
	@Transactional
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		purchaseDao.updateCheckoutUser(event.getFromUserId(), event.getToUserId());
		purchaseDao.updatePaidByUser(event.getFromUserId(), event.getToUserId());
	}

	@Override
	public void execute()
	{
		startCheckDownloadableContentAndCheckSubscriptions();
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		fileProcessorTracker = new PluginTracker<CloneFileProcessingExtension>(pluginService,
			AbstractCloneOperation.class, "cloneFileProcessor", "id");
		fileProcessorTracker.setBeanKey("bean");
	}

	public synchronized String getTaskId()
	{
		if( taskId == null )
		{
			taskId = taskService
				.getGlobalTask(new BeanClusteredTask("CheckPurchasesTask", PurchaseService.class, "createTask"),
					DEFAULT_TIMEOUT)
				.getTaskId();
		}
		return taskId;
	}
}
