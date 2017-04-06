package com.tle.core.payment.service;

import java.util.List;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.CatalogueAssignment;
import com.tle.common.payment.entity.StoreFront;
import com.tle.common.search.PresetSearch;
import com.tle.core.payment.service.session.CatalogueEditingSession.CatalogueEditingBean;
import com.tle.core.services.entity.AbstractEntityService;

/**
 * @author Aaron
 */
public interface CatalogueService extends AbstractEntityService<CatalogueEditingBean, Catalogue>
{
	boolean addItemToList(Long catalogueId, Item item, boolean blacklist);

	boolean removeItemFromList(Long catalogueId, Item item, boolean blacklist);

	List<CatalogueAssignment> listCataloguesForItem(Item item);

	List<CatalogueAssignment> listItemsForCatalogue(Catalogue cat);

	/**
	 * Note that an item can appear in the whitelist as well as dynamic
	 * 
	 * @param item
	 * @return
	 */
	CatalogueInfo groupCataloguesForItem(Item item);

	List<Catalogue> enumerateForCountry(String country);

	PresetSearch createSearch(String catalogueUuid, boolean excludeWhitelist);

	PresetSearch createSearch(Catalogue catalogue, boolean excludeWhitelist);

	PresetSearch createLiveSearch(String catalogueUuid, StoreFront storefront);

	PresetSearch createLiveSearch(String catalogueUuid);

	/**
	 * Contains an item either dynamically or in whitelist. Use only for store
	 * front
	 * 
	 * @param catalogueUuid
	 * @param itemId
	 * @return
	 */
	boolean containsLiveItem(String catalogueUuid, ItemKey itemId, StoreFront storefront);

	List<BaseEntityLabel> listManageable();

	boolean canManage(BaseEntityLabel entity);

	boolean canManage(Catalogue entity);

	public interface CatalogueInfo
	{
		Item getItem();

		List<Catalogue> getWhitelist();

		List<Catalogue> getBlacklist();

		/**
		 * Dynamic AND whitelisted
		 */
		List<Catalogue> getDynamic();

		List<Catalogue> getDynamicExWhitelist();

		List<Catalogue> getNone();
	}

}
