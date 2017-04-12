package com.tle.core.payment.storefront.service;

import java.util.List;

import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.payment.storefront.service.session.StoreEditingBean;
import com.tle.core.services.entity.AbstractEntityService;

public interface StoreService extends AbstractEntityService<StoreEditingBean, Store>
{
	List<Store> enumerateBrowsable();

	void updateHarvestDate(Store store);

	List<Store> findAll();

	/**
	 * Determine if a store has seen any activity, to wit do any OrderStoreParts
	 * exist which reference this store.
	 * 
	 * @param boolean
	 * @return
	 */
	boolean storeHasHistory(Store store);

	/**
	 * @param url
	 * @param id Don't compare to entity with this id
	 * @return
	 */
	boolean isUrlRegistered(String url, long id);
}
