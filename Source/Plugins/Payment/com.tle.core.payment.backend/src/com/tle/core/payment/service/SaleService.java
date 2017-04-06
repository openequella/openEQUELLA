package com.tle.core.payment.service;

import java.util.List;

import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.SaleItem;
import com.tle.common.payment.entity.StoreFront;
import com.tle.core.payment.SaleSearchResults;

/**
 * storefrontUuids must be supplied as a "security" measure.
 * 
 * @author Aaron
 */
public interface SaleService
{
	void checkout(StoreFront storefront, Sale sale);

	void commit(StoreFront storefront, Sale sale, String receipt);

	Sale getSale(StoreFront storefront, String saleUuid);

	Sale getByReceipt(String receipt);

	List<Sale> enumerateForStoreFront(StoreFront storefront);

	SaleSearchResults search(StoreFront storefront, int offset, int count, String customerReference);

	void setPending(StoreFront storefront, Sale sale);

	List<SaleItem> getSalesItemsForSourceItem(String itemUuid);

}
