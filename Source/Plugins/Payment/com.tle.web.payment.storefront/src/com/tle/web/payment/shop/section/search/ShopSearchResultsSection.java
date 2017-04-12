package com.tle.web.payment.shop.section.search;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.exceptions.NotFoundException;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.common.search.DefaultSearch;
import com.tle.core.payment.beans.store.StoreCatalogueItemBean;
import com.tle.core.payment.storefront.service.ShopSearchResults;
import com.tle.core.payment.storefront.service.ShopService;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.payment.shop.event.ShopSearchEvent;
import com.tle.web.payment.shop.event.ShopSearchResultEvent;
import com.tle.web.payment.shop.section.search.ShopSearchResultsSection.ShopSearchResultsModel;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.PagingSection;

/**
 * @author Aaron & Dustin
 */
public class ShopSearchResultsSection
	extends
		AbstractSearchResultsSection<ShopSearchListEntry, ShopSearchEvent, ShopSearchResultEvent, ShopSearchResultsModel>
{
	@Inject
	private ShopService shopService;
	@Inject
	private ShopSearchListEntrySection list;
	@Inject
	private ShopSearchListEntryFactory entryFactory;
	@TreeLookup
	private RootShopSearchSection root;
	@TreeLookup
	private PagingSection<ShopSearchEvent, ShopSearchResultEvent> pagingSection;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(list, id);
	}

	@Override
	public void processResults(SectionInfo info, ShopSearchResultEvent results)
	{
		List<StoreCatalogueItemBean> searchResults = results.getResults().getResults();

		for( StoreCatalogueItemBean storeCatalogueItemBean : searchResults )
		{
			list.addListItem(info,
				entryFactory.createListEntry(info, results.getStore(), results.getCatUuid(), storeCatalogueItemBean));
		}

		Collection<String> words = new DefaultSearch.QueryParser(results.getEvent().getQuery()).getHilightedList();
		ListSettings<ShopSearchListEntry> listSettings = list.getListSettings(info);
		listSettings.setHilightedWords(words);
	}

	@Override
	protected ShopSearchResultEvent createResultsEvent(SectionInfo info, ShopSearchEvent searchEvent)
	{

		final ShopSearchSectionInfo shopInfo = ShopSearchSectionInfo.getSearchInfo(info);
		final Store store = shopInfo.getStore();
		final String catalogue = shopInfo.getCatUuid();

		int amount = pagingSection.getDefaultPerPage();
		int start = (pagingSection.getPager().getCurrentPage(info) - 1) * amount;

		try
		{
			final ShopSearchResults results = shopService.searchCatalogue(store, catalogue, searchEvent.getQuery(),
				searchEvent.getSort(), searchEvent.isReverse(), searchEvent.getDaterange(),
				searchEvent.getPriceFilter(), start, amount);
			final ShopSearchResultEvent event = new ShopSearchResultEvent(searchEvent, store, catalogue, results,
				results.getFilteredOut());
			return event;
		}
		catch( NotFoundException nf )
		{
			// Forces rebuild of catalogue cache
			shopService.getCatalogues(store, true);
			throw nf;
			// ShopSearchResultEvent event = new ShopSearchResultEvent(store,
			// catalogue, null, 0);
			// event.setErrored(true);
			// event.setErrorMessage(nf.getMessage());
			// return event;
		}
	}

	@Override
	public ShopSearchEvent createSearchEvent(SectionInfo info)
	{
		// if( getModel(info).isSearch() )
		{
			return new ShopSearchEvent(root);
		}
		// return null;
	}

	@Override
	public void startSearch(SectionInfo info)
	{
		super.startSearch(info);
		getModel(info).setSearch(true);
	}

	@Override
	public ShopSearchListEntrySection getItemList(SectionInfo info)
	{
		return list;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ShopSearchResultsModel();
	}

	public static class ShopSearchResultsModel extends AbstractSearchResultsSection.SearchResultsModel
	{
		@Bookmarked
		private boolean search;

		public boolean isSearch()
		{
			return search;
		}

		public void setSearch(boolean search)
		{
			this.search = search;
		}
	}

	@Override
	public Class<ShopSearchResultsModel> getModelClass()
	{
		return ShopSearchResultsModel.class;
	}
}
