package com.tle.core.payment.storefront.service;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.common.payment.storefront.entity.OrderStorePart;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.payment.beans.store.StoreBean;
import com.tle.core.payment.beans.store.StoreCatalogueBean;
import com.tle.core.payment.beans.store.StoreCatalogueItemBean;
import com.tle.core.payment.beans.store.StoreCheckoutBean;
import com.tle.core.payment.beans.store.StoreHarvestableItemBean;
import com.tle.core.payment.beans.store.StoreHarvestableItemsBean;
import com.tle.core.payment.beans.store.StorePaymentGatewayBean;
import com.tle.core.payment.beans.store.StorePricingInformationBean;
import com.tle.core.payment.beans.store.StoreTaxBean;
import com.tle.core.payment.beans.store.StoreTransactionBean;
import com.tle.core.payment.storefront.exception.OrderValueChangedException;

/**
 * Acts as a bridge to the backend store
 * 
 * @author Aaron
 */
public interface ShopService
{
	String readToken(String storeUrl, String clientId, String ourUrl, String code);

	boolean testStoreUrl(String url);

	void clearCache(Store store);

	ShopSearchResults searchCatalogue(Store store, String catUuid, String query, String sort, boolean reverse,
		Date[] dateRange, String filter, int start, int amount);

	StoreCatalogueItemBean getCatalogueItem(Store store, String catUuid, String itemUuid);

	/**
	 * @param storeUuid
	 * @param fresh Invalidate the cache and get it from the actual store
	 * @return
	 */
	List<StoreCatalogueBean> getCatalogues(Store store, boolean fresh);

	StoreCatalogueBean getCatalogue(Store store, String uuid, boolean fresh);

	/**
	 * @param storeUuid
	 * @param fresh Invalidate the cache and get it from the actual store
	 * @return
	 */
	StoreBean getStoreInformation(Store store, boolean fresh);

	/**
	 * Do not use. Only for initial connection.
	 * 
	 * @param storeUrl
	 * @param token
	 * @return
	 */
	StoreBean getStoreInformation(String storeUrl, String token);

	/**
	 * @param storeUuid
	 * @param all, get the pricing info and the gateway info
	 * @return
	 */
	StorePricingInformationBean getPricingInformation(Store store, boolean all);

	/**
	 * @param store
	 * @param gatewayUuid
	 * @param fresh
	 * @return
	 */
	StorePaymentGatewayBean getPaymentGateway(Store store, String gatewayUuid, boolean fresh);

	/**
	 * @param store
	 * @param orderPart
	 * @return The store assigned UUID
	 * @throws OrderValueChangedException Thrown when an order is recalculated
	 *             and a value is found to be different
	 */
	StoreCheckoutBean submitOrder(Store store, OrderStorePart orderPart, StoreTaxBean tax)
		throws OrderValueChangedException;

	/**
	 * @param store
	 * @param transactionUuid
	 * @return
	 */
	StoreTransactionBean getTransaction(Store store, String transactionUuid);

	/**
	 * Follow the status of the submitted order
	 * 
	 * @param store
	 * @param checkoutUuid
	 * @return
	 */
	StoreCheckoutBean getCheckout(Store store, String checkoutUuid);

	/**
	 * @param storeUuid
	 * @return A list of new items to harvest
	 */
	StoreHarvestableItemsBean listHarvestableItems(Store store, Date from, int start, int length, boolean onlyNew);

	StoreHarvestableItemsBean listExpiredItems(Store store, Date from, Date until, int start, int length);

	StoreHarvestableItemsBean listActiveItems(Store store, Date from, Date until, int start, int length);

	void downloadItemFiles(Store store, StagingFile staging, StoreHarvestableItemBean item);

	StoreHarvestableItemBean getFullItem(Store store, String uuid);

	/**
	 * Tries to getFullItem and returns false if a 402 (Payment required) is
	 * returned.
	 * 
	 * @param store
	 * @param uuid
	 * @return
	 */
	boolean isHarvestable(Store store, String uuid);

	void getPreviewAttachment(Store store, String catUuid, String itemUuid, String attachmentUuid,
		HttpServletRequest request, HttpServletResponse response);
}
